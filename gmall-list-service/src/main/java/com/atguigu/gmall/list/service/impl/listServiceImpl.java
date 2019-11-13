package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
 class ListServiceImpl implements ListService {

    @Autowired
    private JestClient jestClient;

    @Autowired
    private RedisUtil redisUtil;

    public static final String ES_INDEX="gmall";

    public static final String ES_TYPE="SkuInfo";


    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {

        /*
        1.  明确保存对象
            PUT /INDEX/TYPE/ID
        2.  执行动作

         */
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {
    	/*
		1.	定义dsl 语句
		2.	定义执行的动作
		3.	执行动作并获取返回结果
		 */
        // 制作dsl 语句
        String query = makeQueryStringForSearch(skuLsParams);

        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 制作返回结果集
        SkuLsResult skuLsResult = makeResultForSearch(searchResult,skuLsParams);
        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) {
        //获取reids
        Jedis jedis = redisUtil.getJedis();
        //定义key
        String key ="hotScore";
        //使用数据类型
        Double count = jedis.zincrby(key, 1, "skuId:" + skuId);
        //当商品被访问十次时，更新es
        if(count%10==0){
            updateHotScore(skuId,  Math.round(count));
        }
    }

    private void updateHotScore(String skuId, long hotScore) {

        String updateJson="{\n" +
                "   \"doc\":{\n" +
                "     \"hotScore\":"+hotScore+"\n" +
                "   }\n" +
                "}";
        Update update  = new Update.Builder(updateJson).index(ES_INDEX).type(ES_TYPE).id(skuId).build();

        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 制作返回集结果
     * @param searchResult
     * @param skuLsParams
     * @return
     */
    private SkuLsResult makeResultForSearch(SearchResult searchResult, SkuLsParams skuLsParams) {
        SkuLsResult skuLsResult = new SkuLsResult();
//        List<SkuLsInfo> skuLsInfoList;
        ArrayList<SkuLsInfo> skuLsInfoArrayList = new ArrayList<>();
        // 将集合赋值skuLsInfoArrayList 从searchResult获取
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            // 获取skuLsInfo
            SkuLsInfo skuLsInfo = hit.source;
            // skuLsInfo 中的skuName 并不是高亮！
            // 从highlight 获取高亮
            if (hit.highlight!=null &&hit.highlight.size()>0){
                List<String> list = hit.highlight.get("skuName");
                String skuNameHI = list.get(0);
                // 将原来的skuName 替换成高亮的skuName!
                skuLsInfo.setSkuName(skuNameHI);
            }
            // 将对象放入集合
            skuLsInfoArrayList.add(skuLsInfo);
        }

        skuLsResult.setSkuLsInfoList(skuLsInfoArrayList);
//        long total;
        skuLsResult.setTotal(searchResult.getTotal());
//        long totalPages;
        // 10 3 4 | 9 3 3
        // long totalPages= searchResult.getTotal()%skuLsParams.getPageSize()==0?searchResult.getTotal()/skuLsParams.getPageSize():searchResult.getTotal()/skuLsParams.getPageSize()+1;
        // Math.round(12.5); 13  Math.round(-12.5); -12
        //  long totalPages =  Math.round(searchResult.getTotal()/skuLsParams.getPageSize());
        // 计算总页数！
        long totalPages=(searchResult.getTotal()+skuLsParams.getPageSize()-1)/skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPages);
//        List<String> attrValueIdList;

        ArrayList<String> strValueIdList = new ArrayList<>();
        // strValueIdList 赋值 通过聚合获取平台属性值Id
        MetricAggregation aggregations = searchResult.getAggregations();

        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");

        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        for (TermsAggregation.Entry bucket : buckets) {
            String valueId = bucket.getKey();
            strValueIdList.add(valueId);
        }
        skuLsResult.setAttrValueIdList(strValueIdList);
        return skuLsResult;
    }

    /**
     * 自定义dsl 语句
     * @param skuLsParams
     * @return
     */
    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {
        // 创建查询构造器{ }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 创建 { bool }
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // keyword = skuName
        if (skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            // "skuName": "小米手机"
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",skuLsParams.getKeyword());
            // must -- match --- skuName
            boolQueryBuilder.must(matchQueryBuilder);

            // 设置高亮
            HighlightBuilder highlighter = searchSourceBuilder.highlighter();

            // 设置高亮的字段，前缀，后缀
            highlighter.field("skuName");
            highlighter.preTags("<span style=color:red>");
            highlighter.postTags("</span>");

            // 将高亮对象放入查询器
            searchSourceBuilder.highlight(highlighter);

        }
        //        boolQueryBuilder.filter(); 三级分类Id，平台属性值Id
        // {"term": {"catalog3Id": "61"}
        if (skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            // 循环判断有多少个valueId ,
            // {"term": {"skuAttrValueList.valueId": "80"}
            for (String valueId : skuLsParams.getValueId()) {
                // 有多少valueId 放入多少！
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId",valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        // 调用query方法{ query -- bool }
        searchSourceBuilder.query(boolQueryBuilder);
        // 排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        // 分页：
        // from size
        // 10 3 第一页：0 ,3  第二页：3 ，3 第三页：6 ，3 第四页 9，3
        int from = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        // 默认每页显示20条数据：
        searchSourceBuilder.size(skuLsParams.getPageSize());
        // 聚合：  "field": "skuAttrValueList.valueId"
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);

        String query = searchSourceBuilder.toString();

        System.out.println("query:="+query);
        return query;

    }
}