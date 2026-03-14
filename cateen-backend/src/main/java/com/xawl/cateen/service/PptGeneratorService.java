package com.xawl.cateen.service;

import com.xawl.cateen.entity.Comment;
import com.xawl.cateen.entity.Food;
import com.xawl.cateen.entity.ForumPost;
import com.xawl.cateen.entity.Profile;
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
import java.util.Arrays;
import java.util.List;

/**
 * PPT 生成服务
 * 支持单表或多表组合，每个表对应若干幻灯片页
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

    private static final Color COLOR_PRIMARY = new Color(41, 128, 185);
    private static final Color COLOR_BG     = new Color(236, 240, 241);
    private static final Color COLOR_WHITE  = Color.WHITE;
    private static final Color COLOR_DARK   = new Color(44, 62, 80);

    /**
     * 根据类型列表生成 PPT，每个类型对应一组幻灯片
     * @param types 逗号分隔，可选：users, foods, comments, posts
     */
    public String generate(String types) throws Exception {
        List<String> typeList = Arrays.stream(types.split(","))
            .map(String::trim).filter(s -> !s.isEmpty()).toList();

        if (typeList.isEmpty()) {
            throw new IllegalArgumentException("请指定至少一个类型：users/foods/comments/posts");
        }

        try (XMLSlideShow ppt = new XMLSlideShow()) {
            ppt.setPageSize(new Dimension(960, 540));

            // 封面
            String title = buildTitle(typeList);
            addTitleSlide(ppt, title, "生成时间：" + LocalDateTime.now().format(FMT));

            // 如果包含多个类型，先加一页汇总概览
            if (typeList.size() > 1) {
                addSummarySlide(ppt, typeList);
            }

            // 各类型详情页
            for (String type : typeList) {
                switch (type) {
                    case "users"    -> addUserSlides(ppt);
                    case "foods"    -> addFoodSlides(ppt);
                    case "comments" -> addCommentSlides(ppt);
                    case "posts"    -> addPostSlides(ppt);
                    default -> throw new IllegalArgumentException("不支持的类型：" + type);
                }
            }

            String name = String.join("+", typeList) + "_报告";
            return saveFile(ppt, name);
        }
    }

    // ---- 各类型幻灯片 ----

    private void addUserSlides(XMLSlideShow ppt) {
        List<Profile> users = profileMapper.selectList(
            new LambdaQueryWrapper<Profile>().orderByDesc(Profile::getCreatedAt).last("LIMIT 10")
        );
        Long total = profileMapper.selectCount(null);

        XSLFSlide slide = ppt.createSlide();
        fillBackground(slide, COLOR_BG);
        addSlideTitle(slide, "用户数据  （总计 " + total + " 人）");

        String[] headers = {"用户ID", "昵称", "注册时间"};
        int[] colX = {30, 230, 580};
        int[] colW = {190, 340, 320};
        addTableRow(slide, 100, headers, colX, colW, true);

        int y = 140;
        for (Profile u : users) {
            String[] row = {nvl(u.getId()), nvl(u.getNickname()),
                u.getCreatedAt() != null ? u.getCreatedAt().format(FMT) : ""};
            addTableRow(slide, y, row, colX, colW, false);
            y += 36;
        }
    }

    private void addFoodSlides(XMLSlideShow ppt) {
        List<Food> foods = foodMapper.selectList(
            new LambdaQueryWrapper<Food>().eq(Food::getStatus, "active")
                .orderByDesc(Food::getRating).last("LIMIT 8")
        );
        Long total = foodMapper.selectCount(null);

        XSLFSlide slide = ppt.createSlide();
        fillBackground(slide, COLOR_BG);
        addSlideTitle(slide, "美食数据  （在售 " + total + " 种）");

        String[] headers = {"名称", "评分", "评价人数", "价格(元)", "状态"};
        int[] colX = {30, 330, 440, 570, 700};
        int[] colW = {290, 100, 120, 120, 100};
        addTableRow(slide, 100, headers, colX, colW, true);

        int y = 140;
        for (Food f : foods) {
            String[] row = {
                nvl(f.getName()),
                f.getRating() != null ? String.format("%.1f", f.getRating().doubleValue()) : "0.0",
                String.valueOf(f.getRatingCount() != null ? f.getRatingCount() : 0),
                f.getPrice() != null ? String.format("%.2f", f.getPrice().doubleValue()) : "0.00",
                nvl(f.getStatus())
            };
            addTableRow(slide, y, row, colX, colW, false);
            y += 36;
        }
    }

    private void addCommentSlides(XMLSlideShow ppt) {
        List<Comment> comments = commentMapper.selectList(
            new LambdaQueryWrapper<Comment>().orderByDesc(Comment::getCreatedAt).last("LIMIT 8")
        );
        Long total = commentMapper.selectCount(null);

        XSLFSlide slide = ppt.createSlide();
        fillBackground(slide, COLOR_BG);
        addSlideTitle(slide, "评论数据  （总计 " + total + " 条）");

        String[] headers = {"用户ID", "美食ID", "评分", "内容摘要", "时间"};
        int[] colX = {30, 200, 370, 440, 700};
        int[] colW = {160, 160, 60, 250, 200};
        addTableRow(slide, 100, headers, colX, colW, true);

        int y = 140;
        for (Comment c : comments) {
            String preview = c.getContent() != null && c.getContent().length() > 20
                ? c.getContent().substring(0, 20) + "..." : nvl(c.getContent());
            String[] row = {
                nvl(c.getUserId()), nvl(c.getFoodId()),
                c.getRating() != null ? String.format("%.1f", c.getRating().doubleValue()) : "0.0",
                preview,
                c.getCreatedAt() != null ? c.getCreatedAt().format(FMT) : ""
            };
            addTableRow(slide, y, row, colX, colW, false);
            y += 36;
        }
    }

    private void addPostSlides(XMLSlideShow ppt) {
        List<ForumPost> posts = forumPostMapper.selectList(
            new LambdaQueryWrapper<ForumPost>().orderByDesc(ForumPost::getCreatedAt).last("LIMIT 8")
        );
        Long total = forumPostMapper.selectCount(null);

        XSLFSlide slide = ppt.createSlide();
        fillBackground(slide, COLOR_BG);
        addSlideTitle(slide, "帖子数据  （总计 " + total + " 条）");

        String[] headers = {"内容摘要", "点赞", "评论", "状态", "发布时间"};
        int[] colX = {30, 480, 560, 650, 760};
        int[] colW = {440, 70, 80, 100, 170};
        addTableRow(slide, 100, headers, colX, colW, true);

        int y = 140;
        for (ForumPost p : posts) {
            String preview = p.getContent() != null && p.getContent().length() > 28
                ? p.getContent().substring(0, 28) + "..." : nvl(p.getContent());
            String[] row = {
                preview,
                String.valueOf(p.getLikeCount() != null ? p.getLikeCount() : 0),
                String.valueOf(p.getCommentCount() != null ? p.getCommentCount() : 0),
                nvl(p.getStatus()),
                p.getCreatedAt() != null ? p.getCreatedAt().format(FMT) : ""
            };
            addTableRow(slide, y, row, colX, colW, false);
            y += 36;
        }
    }

    /** 多类型时的汇总概览页 */
    private void addSummarySlide(XMLSlideShow ppt, List<String> types) {
        XSLFSlide slide = ppt.createSlide();
        fillBackground(slide, COLOR_BG);
        addSlideTitle(slide, "数据概览");

        String[][] allCards = {
            {"users",    "用户总数",  String.valueOf(profileMapper.selectCount(null))},
            {"foods",    "美食总数",  String.valueOf(foodMapper.selectCount(null))},
            {"comments", "评论总数",  String.valueOf(commentMapper.selectCount(null))},
            {"posts",    "帖子总数",  String.valueOf(forumPostMapper.selectCount(null))}
        };

        List<String[]> selected = Arrays.stream(allCards)
            .filter(c -> types.contains(c[0])).toList();

        int total = selected.size();
        int cardW = 160;
        int gap = 20;
        int totalWidth = total * cardW + (total - 1) * gap;
        int startX = (960 - totalWidth) / 2;

        for (int i = 0; i < selected.size(); i++) {
            int x = startX + i * (cardW + gap);
            addDataCard(slide, x, 150, cardW, 180, selected.get(i)[1], selected.get(i)[2]);
        }
    }

    // ---- 幻灯片构建工具 ----

    private void addTitleSlide(XMLSlideShow ppt, String title, String subtitle) {
        XSLFSlide slide = ppt.createSlide();
        fillBackground(slide, COLOR_PRIMARY);

        XSLFTextBox tb = slide.createTextBox();
        tb.setAnchor(new Rectangle(60, 160, 840, 120));
        XSLFTextParagraph tp = tb.addNewTextParagraph();
        tp.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun tr = tp.addNewTextRun();
        tr.setText(title);
        tr.setFontSize(36.0);
        tr.setBold(true);
        tr.setFontColor(COLOR_WHITE);

        XSLFTextBox sb = slide.createTextBox();
        sb.setAnchor(new Rectangle(60, 300, 840, 60));
        XSLFTextParagraph sp = sb.addNewTextParagraph();
        sp.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun sr = sp.addNewTextRun();
        sr.setText(subtitle);
        sr.setFontSize(16.0);
        sr.setFontColor(new Color(189, 215, 238));
    }

    private void fillBackground(XSLFSlide slide, Color color) {
        XSLFAutoShape bg = slide.createAutoShape();
        bg.setShapeType(org.apache.poi.sl.usermodel.ShapeType.RECT);
        bg.setAnchor(new Rectangle(0, 0, 960, 540));
        bg.setFillColor(color);
        bg.setLineColor(color);
    }

    private void addSlideTitle(XSLFSlide slide, String title) {
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
        tr.setFontSize(24.0);
        tr.setBold(true);
        tr.setFontColor(COLOR_WHITE);
    }

    private void addDataCard(XSLFSlide slide, int x, int y, int w, int h, String label, String value) {
        XSLFAutoShape card = slide.createAutoShape();
        card.setShapeType(org.apache.poi.sl.usermodel.ShapeType.RECT);
        card.setAnchor(new Rectangle(x, y, w, h));
        card.setFillColor(COLOR_WHITE);
        card.setLineColor(new Color(189, 195, 199));

        XSLFTextBox valBox = slide.createTextBox();
        valBox.setAnchor(new Rectangle(x, y + 30, w, 70));
        XSLFTextParagraph vp = valBox.addNewTextParagraph();
        vp.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun vr = vp.addNewTextRun();
        vr.setText(value);
        vr.setFontSize(34.0);
        vr.setBold(true);
        vr.setFontColor(COLOR_PRIMARY);

        XSLFTextBox lblBox = slide.createTextBox();
        lblBox.setAnchor(new Rectangle(x, y + 110, w, 40));
        XSLFTextParagraph lp = lblBox.addNewTextParagraph();
        lp.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun lr = lp.addNewTextRun();
        lr.setText(label);
        lr.setFontSize(14.0);
        lr.setFontColor(COLOR_DARK);
    }

    private void addTableRow(XSLFSlide slide, int y, String[] cells, int[] colX, int[] colW, boolean isHeader) {
        for (int i = 0; i < cells.length; i++) {
            XSLFTextBox box = slide.createTextBox();
            box.setAnchor(new Rectangle(colX[i], y, colW[i], 34));
            if (isHeader) box.setFillColor(COLOR_PRIMARY);
            XSLFTextParagraph tp = box.addNewTextParagraph();
            XSLFTextRun tr = tp.addNewTextRun();
            tr.setText(cells[i]);
            tr.setFontSize(12.0);
            tr.setBold(isHeader);
            tr.setFontColor(isHeader ? COLOR_WHITE : COLOR_DARK);
        }
    }

    private String buildTitle(List<String> types) {
        return types.stream().map(t -> switch (t) {
            case "users"    -> "用户";
            case "foods"    -> "美食";
            case "comments" -> "评论";
            case "posts"    -> "帖子";
            default         -> t;
        }).reduce((a, b) -> a + " + " + b).orElse("数据") + " 报告";
    }

    private String nvl(String s) { return s != null ? s : ""; }

    private String saveFile(XMLSlideShow ppt, String name) throws Exception {
        String dir = uploadPath + "/exports";
        new File(dir).mkdirs();
        String filename = name + "_" + LocalDateTime.now().format(FILE_FMT) + ".pptx";
        try (FileOutputStream fos = new FileOutputStream(dir + "/" + filename)) {
            ppt.write(fos);
        }
        log.info("PPT 生成成功：{}/{}", dir, filename);
        return baseUrl + "/exports/" + filename;
    }
}
