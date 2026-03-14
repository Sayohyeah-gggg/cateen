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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Excel 生成服务
 * 支持单表或多表组合，每个表对应一个 Sheet
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelGeneratorService {

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

    /**
     * 根据类型列表生成 Excel，每个类型对应一个 Sheet
     * @param types 逗号分隔，可选：users, foods, comments, posts
     */
    public String generate(String types) throws Exception {
        List<String> typeList = Arrays.stream(types.split(","))
            .map(String::trim).filter(s -> !s.isEmpty()).toList();

        if (typeList.isEmpty()) {
            throw new IllegalArgumentException("请指定至少一个类型：users/foods/comments/posts");
        }

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(wb);

            for (String type : typeList) {
                switch (type) {
                    case "users"    -> fillUserSheet(wb, headerStyle);
                    case "foods"    -> fillFoodSheet(wb, headerStyle);
                    case "comments" -> fillCommentSheet(wb, headerStyle);
                    case "posts"    -> fillPostSheet(wb, headerStyle);
                    default -> throw new IllegalArgumentException("不支持的类型：" + type);
                }
            }

            String sheetNames = String.join("+", typeList);
            return saveFile(wb, sheetNames);
        }
    }

    // ---- Sheet 填充方法 ----

    private void fillUserSheet(XSSFWorkbook wb, CellStyle headerStyle) {
        List<Profile> users = profileMapper.selectList(
            new LambdaQueryWrapper<Profile>().orderByDesc(Profile::getCreatedAt)
        );
        Sheet sheet = wb.createSheet("用户列表");
        String[] headers = {"用户ID", "昵称", "注册时间"};
        createHeaderRow(sheet, headers, headerStyle);
        int rowIdx = 1;
        for (Profile u : users) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(nvl(u.getId()));
            row.createCell(1).setCellValue(nvl(u.getNickname()));
            row.createCell(2).setCellValue(u.getCreatedAt() != null ? u.getCreatedAt().format(FMT) : "");
        }
        autoSizeColumns(sheet, headers.length);
    }

    private void fillFoodSheet(XSSFWorkbook wb, CellStyle headerStyle) {
        List<Food> foods = foodMapper.selectList(
            new LambdaQueryWrapper<Food>().orderByDesc(Food::getRating)
        );
        Sheet sheet = wb.createSheet("美食列表");
        String[] headers = {"美食ID", "名称", "价格(元)", "评分", "评价人数", "状态"};
        createHeaderRow(sheet, headers, headerStyle);
        int rowIdx = 1;
        for (Food f : foods) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(nvl(f.getId()));
            row.createCell(1).setCellValue(nvl(f.getName()));
            row.createCell(2).setCellValue(f.getPrice() != null ? f.getPrice().doubleValue() : 0);
            row.createCell(3).setCellValue(f.getRating() != null ? f.getRating().doubleValue() : 0);
            row.createCell(4).setCellValue(f.getRatingCount() != null ? f.getRatingCount() : 0);
            row.createCell(5).setCellValue(nvl(f.getStatus()));
        }
        autoSizeColumns(sheet, headers.length);
    }

    private void fillCommentSheet(XSSFWorkbook wb, CellStyle headerStyle) {
        List<Comment> comments = commentMapper.selectList(
            new LambdaQueryWrapper<Comment>().orderByDesc(Comment::getCreatedAt).last("LIMIT 500")
        );
        Sheet sheet = wb.createSheet("评论列表");
        String[] headers = {"评论ID", "用户ID", "美食ID", "评分", "内容", "时间"};
        createHeaderRow(sheet, headers, headerStyle);
        int rowIdx = 1;
        for (Comment c : comments) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(nvl(c.getId()));
            row.createCell(1).setCellValue(nvl(c.getUserId()));
            row.createCell(2).setCellValue(nvl(c.getFoodId()));
            row.createCell(3).setCellValue(c.getRating() != null ? c.getRating().doubleValue() : 0);
            row.createCell(4).setCellValue(nvl(c.getContent()));
            row.createCell(5).setCellValue(c.getCreatedAt() != null ? c.getCreatedAt().format(FMT) : "");
        }
        autoSizeColumns(sheet, headers.length);
    }

    private void fillPostSheet(XSSFWorkbook wb, CellStyle headerStyle) {
        List<ForumPost> posts = forumPostMapper.selectList(
            new LambdaQueryWrapper<ForumPost>().orderByDesc(ForumPost::getCreatedAt).last("LIMIT 500")
        );
        Sheet sheet = wb.createSheet("帖子列表");
        String[] headers = {"帖子ID", "用户ID", "内容摘要", "点赞数", "评论数", "状态", "发布时间"};
        createHeaderRow(sheet, headers, headerStyle);
        int rowIdx = 1;
        for (ForumPost p : posts) {
            Row row = sheet.createRow(rowIdx++);
            String preview = p.getContent() != null && p.getContent().length() > 50
                ? p.getContent().substring(0, 50) + "..." : nvl(p.getContent());
            row.createCell(0).setCellValue(nvl(p.getId()));
            row.createCell(1).setCellValue(nvl(p.getUserId()));
            row.createCell(2).setCellValue(preview);
            row.createCell(3).setCellValue(p.getLikeCount() != null ? p.getLikeCount() : 0);
            row.createCell(4).setCellValue(p.getCommentCount() != null ? p.getCommentCount() : 0);
            row.createCell(5).setCellValue(nvl(p.getStatus()));
            row.createCell(6).setCellValue(p.getCreatedAt() != null ? p.getCreatedAt().format(FMT) : "");
        }
        autoSizeColumns(sheet, headers.length);
    }

    // ---- 工具方法 ----

    private String nvl(String s) { return s != null ? s : ""; }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private void createHeaderRow(Sheet sheet, String[] headers, CellStyle style) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) < 3000) sheet.setColumnWidth(i, 3000);
        }
    }

    private String saveFile(XSSFWorkbook wb, String name) throws Exception {
        String dir = uploadPath + "/exports";
        new File(dir).mkdirs();
        String filename = name + "_" + LocalDateTime.now().format(FILE_FMT) + ".xlsx";
        try (FileOutputStream fos = new FileOutputStream(dir + "/" + filename)) {
            wb.write(fos);
        }
        log.info("Excel 生成成功：{}/{}", dir, filename);
        return baseUrl + "/exports/" + filename;
    }
}
