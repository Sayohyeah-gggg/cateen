package com.xawl.cateen.service;

import com.xawl.cateen.entity.Food;
import com.xawl.cateen.entity.ForumPost;
import com.xawl.cateen.mapper.CommentMapper;
import com.xawl.cateen.mapper.FoodMapper;
import com.xawl.cateen.mapper.ForumPostMapper;
import com.xawl.cateen.mapper.ProfileMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PPT 生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PptGeneratorService {

    private final ProfileMapper profileMapper;
    private final FoodMapper foodMapper;
    private final CommentMapper commentMapper;
    private final ForumPostMapper forumPostMapper;

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Value("${upload.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // 主题色
    private static final Color COLOR_PRIMARY = new Color(41, 128, 185);
    private static final Color COLOR_BG = new Color(236, 240, 241);
    private static final Color COLOR_WHITE = Color.WHITE;
    private static final Color COLOR_DARK = new Color(44, 62, 80);

    /**
     * 生成系统数据概览 PPT
     */
    public String generateOverviewPpt() throws Exception {
        Long totalUsers = profileMapper.selectCount(null);
        Long totalFoods = foodMapper.selectCount(null);
        Long totalComments = commentMapper.selectCount(null);
        Long totalPosts = forumPostMapper.selectCount(null);

        List<Food> topFoods = foodMapper.selectList(
            new LambdaQueryWrapper<Food>().eq(Food::getStatus, "active")
                .orderByDesc(Food::getRating).last("LIMIT 5")
        );

        List<ForumPost> recentPosts = forumPostMapper.selectList(
            new LambdaQueryWrapper<ForumPost>().orderByDesc(ForumPost::getCreatedAt).last("LIMIT 5")
        );

        try (XMLSlideShow ppt = new XMLSlideShow()) {
            ppt.setPageSize(new Dimension(960, 540));

            // 封面页
            addTitleSlide(ppt, "食堂管理系统数据报告",
                "生成时间：" + LocalDateTime.now().format(FMT));

            // 数据概览页
            addOverviewSlide(ppt, totalUsers, totalFoods, totalComments, totalPosts);

            // 热门美食页
            addFoodRankSlide(ppt, topFoods);

            // 最新帖子页
            addRecentPostsSlide(ppt, recentPosts);

            return saveFile(ppt, "数据报告");
        }
    }

    // ---- 幻灯片构建方法 ----

    private void addTitleSlide(XMLSlideShow ppt, String title, String subtitle) {
        XSLFSlide slide = ppt.createSlide();
        fillBackground(slide, COLOR_PRIMARY);

        // 主标题
        XSLFTextBox titleBox = slide.createTextBox();
        titleBox.setAnchor(new Rectangle(60, 160, 840, 120));
        XSLFTextParagraph tp = titleBox.addNewTextParagraph();
        tp.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun tr = tp.addNewTextRun();
        tr.setText(title);
        tr.setFontSize(40.0);
        tr.setBold(true);
        tr.setFontColor(COLOR_WHITE);

        // 副标题
        XSLFTextBox subBox = slide.createTextBox();
        subBox.setAnchor(new Rectangle(60, 300, 840, 60));
        XSLFTextParagraph sp = subBox.addNewTextParagraph();
        sp.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun sr = sp.addNewTextRun();
        sr.setText(subtitle);
        sr.setFontSize(18.0);
        sr.setFontColor(new Color(189, 215, 238));
    }

    private void addOverviewSlide(XMLSlideShow ppt, long users, long foods, long comments, long posts) {
        XSLFSlide slide = ppt.createSlide();
        fillBackground(slide, COLOR_BG);

        addSlideTitle(slide, "数据概览");

        // 四个数据卡片
        String[][] cards = {
            {"用户总数", String.valueOf(users)},
            {"美食总数", String.valueOf(foods)},
            {"评论总数", String.valueOf(comments)},
            {"帖子总数", String.valueOf(posts)}
        };

        int startX = 60;
        int cardW = 190;
        int gap = 20;
        for (int i = 0; i < cards.length; i++) {
            int x = startX + i * (cardW + gap);
            addDataCard(slide, x, 140, cardW, 200, cards[i][0], cards[i][1]);
        }
    }

    private void addFoodRankSlide(XMLSlideShow ppt, List<Food> foods) {
        XSLFSlide slide = ppt.createSlide();
        fillBackground(slide, COLOR_BG);

        addSlideTitle(slide, "评分最高的美食 Top 5");

        // 表头
        String[] headers = {"排名", "名称", "评分", "评价人数", "价格(元)"};
        int[] colX = {60, 130, 480, 600, 740};
        int[] colW = {60, 340, 110, 130, 110};

        addTableRow(slide, 130, headers, colX, colW, true);

        int y = 170;
        for (int i = 0; i < foods.size(); i++) {
            Food f = foods.get(i);
            String[] row = {
                String.valueOf(i + 1),
                f.getName() != null ? f.getName() : "",
                f.getRating() != null ? String.format("%.1f", f.getRating().doubleValue()) : "0.0",
                String.valueOf(f.getRatingCount() != null ? f.getRatingCount() : 0),
                f.getPrice() != null ? String.format("%.2f", f.getPrice().doubleValue()) : "0.00"
            };
            addTableRow(slide, y, row, colX, colW, false);
            y += 40;
        }
    }

    private void addRecentPostsSlide(XMLSlideShow ppt, List<ForumPost> posts) {
        XSLFSlide slide = ppt.createSlide();
        fillBackground(slide, COLOR_BG);

        addSlideTitle(slide, "最新帖子");

        String[] headers = {"帖子ID", "内容摘要", "点赞", "评论", "状态"};
        int[] colX = {30, 160, 620, 700, 790};
        int[] colW = {120, 450, 70, 80, 100};

        addTableRow(slide, 130, headers, colX, colW, true);

        int y = 170;
        for (ForumPost p : posts) {
            String preview = p.getContent() != null && p.getContent().length() > 30
                ? p.getContent().substring(0, 30) + "..." : p.getContent();
            String[] row = {
                p.getId() != null && p.getId().length() > 12 ? p.getId().substring(0, 12) + ".." : p.getId(),
                preview != null ? preview : "",
                String.valueOf(p.getLikeCount() != null ? p.getLikeCount() : 0),
                String.valueOf(p.getCommentCount() != null ? p.getCommentCount() : 0),
                p.getStatus() != null ? p.getStatus() : ""
            };
            addTableRow(slide, y, row, colX, colW, false);
            y += 40;
        }
    }

    // ---- 私有工具方法 ----

    private void fillBackground(XSLFSlide slide, Color color) {
        XSLFAutoShape bg = slide.createAutoShape();
        bg.setShapeType(org.apache.poi.sl.usermodel.ShapeType.RECT);
        bg.setAnchor(new Rectangle(0, 0, 960, 540));
        bg.setFillColor(color);
        bg.setLineColor(color);
    }

    private void addSlideTitle(XSLFSlide slide, String title) {
        // 标题背景条
        XSLFAutoShape bar = slide.createAutoShape();
        bar.setShapeType(org.apache.poi.sl.usermodel.ShapeType.RECT);
        bar.setAnchor(new Rectangle(0, 0, 960, 80));
        bar.setFillColor(COLOR_PRIMARY);
        bar.setLineColor(COLOR_PRIMARY);

        XSLFTextBox tb = slide.createTextBox();
        tb.setAnchor(new Rectangle(30, 10, 900, 60));
        XSLFTextParagraph tp = tb.addNewTextParagraph();
        XSLFTextRun tr = tp.addNewTextRun();
        tr.setText(title);
        tr.setFontSize(26.0);
        tr.setBold(true);
        tr.setFontColor(COLOR_WHITE);
    }

    private void addDataCard(XSLFSlide slide, int x, int y, int w, int h, String label, String value) {
        XSLFAutoShape card = slide.createAutoShape();
        card.setShapeType(org.apache.poi.sl.usermodel.ShapeType.RECT);
        card.setAnchor(new Rectangle(x, y, w, h));
        card.setFillColor(COLOR_WHITE);
        card.setLineColor(new Color(189, 195, 199));

        // 数值
        XSLFTextBox valBox = slide.createTextBox();
        valBox.setAnchor(new Rectangle(x, y + 40, w, 80));
        XSLFTextParagraph vp = valBox.addNewTextParagraph();
        vp.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun vr = vp.addNewTextRun();
        vr.setText(value);
        vr.setFontSize(36.0);
        vr.setBold(true);
        vr.setFontColor(COLOR_PRIMARY);

        // 标签
        XSLFTextBox lblBox = slide.createTextBox();
        lblBox.setAnchor(new Rectangle(x, y + 130, w, 40));
        XSLFTextParagraph lp = lblBox.addNewTextParagraph();
        lp.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun lr = lp.addNewTextRun();
        lr.setText(label);
        lr.setFontSize(16.0);
        lr.setFontColor(COLOR_DARK);
    }

    private void addTableRow(XSLFSlide slide, int y, String[] cells, int[] colX, int[] colW, boolean isHeader) {
        for (int i = 0; i < cells.length; i++) {
            XSLFTextBox box = slide.createTextBox();
            box.setAnchor(new Rectangle(colX[i], y, colW[i], 36));
            if (isHeader) {
                box.setFillColor(COLOR_PRIMARY);
            }
            XSLFTextParagraph tp = box.addNewTextParagraph();
            XSLFTextRun tr = tp.addNewTextRun();
            tr.setText(cells[i]);
            tr.setFontSize(13.0);
            tr.setBold(isHeader);
            tr.setFontColor(isHeader ? COLOR_WHITE : COLOR_DARK);
        }
    }

    private String saveFile(XMLSlideShow ppt, String name) throws Exception {
        String dir = uploadPath + "/exports";
        File dirFile = new File(dir);
        if (!dirFile.exists()) dirFile.mkdirs();

        String filename = name + "_" + LocalDateTime.now().format(FILE_FMT) + ".pptx";
        String filePath = dir + "/" + filename;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            ppt.write(fos);
        }

        log.info("PPT 生成成功：{}", filePath);
        return baseUrl + "/exports/" + filename;
    }
}
