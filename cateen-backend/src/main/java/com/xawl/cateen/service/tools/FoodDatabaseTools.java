package com.xawl.cateen.service.tools;

import com.xawl.cateen.service.FoodService;
import com.xawl.cateen.vo.FoodVO;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 美食数据库工具类
 * 使用LangChain4j @Tool注解暴露方法给AI调用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FoodDatabaseTools {

    private final FoodService foodService;

    @Tool("查询热门美食列表，返回评分最高、最受欢迎的美食")
    public String getPopularFoods() {
        try {
            log.info("AI调用工具：查询热门美食");
            List<FoodVO> foods = foodService.getPopularFoods(10);
            
            if (foods == null || foods.isEmpty()) {
                return "暂无热门美食数据";
            }

            StringBuilder result = new StringBuilder("热门美食列表：\n\n");
            for (FoodVO food : foods) {
                result.append(String.format("- %s：%s，价格：%.2f元，评分：%.1f（%d人评价）\n",
                        food.getName(),
                        food.getDescription(),
                        food.getPrice(),
                        food.getRating(),
                        food.getRatingCount()));
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("查询热门美食失败", e);
            return "查询热门美食失败：" + e.getMessage();
        }
    }

    @Tool("根据美食ID查询详细信息，包括名称、描述、价格、评分等")
    public String getFoodDetail(String foodId) {
        try {
            log.info("AI调用工具：查询美食详情，ID={}", foodId);
            FoodVO food = foodService.getFoodDetail(foodId);
            
            if (food == null) {
                return "未找到ID为 " + foodId + " 的美食";
            }

            return String.format("美食详情：\n" +
                    "名称：%s\n" +
                    "描述：%s\n" +
                    "分类：%s\n" +
                    "价格：%.2f元\n" +
                    "评分：%.1f（%d人评价）\n" +
                    "状态：%s",
                    food.getName(),
                    food.getDescription(),
                    food.getCategory() != null ? food.getCategory().getName() : "未分类",
                    food.getPrice(),
                    food.getRating(),
                    food.getRatingCount(),
                    "active".equals(food.getStatus()) ? "可售" : "已下架");
        } catch (Exception e) {
            log.error("查询美食详情失败", e);
            return "查询美食详情失败：" + e.getMessage();
        }
    }

    @Tool("搜索美食，支持按关键词搜索名称或描述")
    public String searchFoods(String keyword) {
        try {
            log.info("AI调用工具：搜索美食，关键词={}", keyword);
            var page = foodService.getFoodPage(1L, 10L, keyword, null, "active");
            
            if (page == null || page.getRecords().isEmpty()) {
                return "未找到包含关键词 \"" + keyword + "\" 的美食";
            }

            StringBuilder result = new StringBuilder(String.format("搜索到 %d 个相关美食：\n\n", page.getTotal()));
            for (FoodVO food : page.getRecords()) {
                result.append(String.format("- %s：%s，价格：%.2f元，评分：%.1f\n",
                        food.getName(),
                        food.getDescription(),
                        food.getPrice(),
                        food.getRating()));
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("搜索美食失败", e);
            return "搜索美食失败：" + e.getMessage();
        }
    }

    @Tool("根据分类ID查询该分类下的所有美食")
    public String getFoodsByCategory(String categoryId) {
        try {
            log.info("AI调用工具：按分类查询美食，分类ID={}", categoryId);
            var page = foodService.getFoodPage(1L, 20L, null, categoryId, "active");
            
            if (page == null || page.getRecords().isEmpty()) {
                return "该分类下暂无美食";
            }

            StringBuilder result = new StringBuilder(String.format("该分类共有 %d 个美食：\n\n", page.getTotal()));
            for (FoodVO food : page.getRecords()) {
                result.append(String.format("- %s：%s，价格：%.2f元，评分：%.1f\n",
                        food.getName(),
                        food.getDescription(),
                        food.getPrice(),
                        food.getRating()));
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("按分类查询美食失败", e);
            return "按分类查询美食失败：" + e.getMessage();
        }
    }

    @Tool("获取高评分美食推荐，返回评分4.5分以上的优质美食")
    public String getHighRatedFoods() {
        try {
            log.info("AI调用工具：查询高评分美食");
            var page = foodService.getFoodPage(1L, 10L, null, null, "active");
            
            if (page == null || page.getRecords().isEmpty()) {
                return "暂无美食数据";
            }

            List<FoodVO> highRated = page.getRecords().stream()
                    .filter(food -> food.getRating() != null && food.getRating().doubleValue() >= 4.5)
                    .collect(Collectors.toList());

            if (highRated.isEmpty()) {
                return "暂无4.5分以上的高评分美食";
            }

            StringBuilder result = new StringBuilder("高评分美食推荐（4.5分以上）：\n\n");
            for (FoodVO food : highRated) {
                result.append(String.format("- %s：%s，价格：%.2f元，评分：%.1f⭐\n",
                        food.getName(),
                        food.getDescription(),
                        food.getPrice(),
                        food.getRating()));
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("查询高评分美食失败", e);
            return "查询高评分美食失败：" + e.getMessage();
        }
    }
}
