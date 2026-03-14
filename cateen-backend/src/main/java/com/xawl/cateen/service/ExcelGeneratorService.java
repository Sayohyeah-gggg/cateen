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
import java.util.List;

/**
 * Excel 生成服务
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
     * 生成用户列表 Excel
     */
    public String generateUserExcel() throws Exception {
        List<Profile> users = profileMapper.selectList(
            new LambdaQueryWrapper<Profile>().orderByDesc(Profile::getCreatedAt)
        );

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("用户列表");
            CellStyle headerStyle = createHeaderStyle(wb);

            // 表头
            String[] headers = {"用户ID", "昵称", "注册时间"};
            createHeaderRow(sheet, headers, headerStyle);

            // 数据行
            int rowIdx = 1;
            for (Profile u : users) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(u.getId() != null ? u.getId() : "");
                row.createCell(1).setCellValue(u.getNickname() != null ? u.getNickname() : "");
                row.createCell(2).setCellValue(u.getCreatedAt() != null ? u.getCreatedAt().format(FMT) : "");
            }

            autoSizeColumns(sheet, headers.length);
            return saveFile(wb, "用户列表");
        }
    }

    /**
     * 生成美食列表 Excel
     */
    public String generateFoodExcel() throws Exception {
        List<Food> foods = foodMapper.selectList(
            new LambdaQueryWrapper<Food>().orderByDesc(Food::getRating)
        );

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("美食列表");
            CellStyle headerStyle = createHeaderStyle(wb);

            String[] headers = {"美食ID", "名称", "价格(元)", "评分", "评价人数", "状态"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (Food f : foods) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(f.getId() != null ? f.getId() : "");
                row.createCell(1).setCellValue(f.getName() != null ? f.getName() : "");
                row.createCell(2).setCellValue(f.getPrice() != null ? f.getPrice().doubleValue() : 0);
                row.createCell(3).setCellValue(f.getRating() != null ? f.getRating().doubleValue() : 0);
                row.createCell(4).setCellValue(f.getRatingCount() != null ? f.getRatingCount() : 0);
                row.createCell(5).setCellValue(f.getStatus() != null ? f.getStatus() : "");
            }

            autoSizeColumns(sheet, headers.length);
            return saveFile(wb, "美食列表");
        }
    }

    /**
     * 生成评论列表 Excel
     */
    public String generateCommentExcel() throws Exception {
        List<Comment> comments = commentMapper.selectList(
            new LambdaQueryWrapper<Comment>().orderByDesc(Comment::getCreatedAt).last("LIMIT 500")
        );

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("评论列表");
            CellStyle headerStyle = createHeaderStyle(wb);

            String[] headers = {"评论ID", "用户ID", "美食ID", "评分", "内容", "时间"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (Comment c : comments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getId() != null ? c.getId() : "");
                row.createCell(1).setCellValue(c.getUserId() != null ? c.getUserId() : "");
                row.createCell(2).setCellValue(c.getFoodId() != null ? c.getFoodId() : "");
                row.createCell(3).setCellValue(c.getRating() != null ? c.getRating().doubleValue() : 0);
                row.createCell(4).setCellValue(c.getContent() != null ? c.getContent() : "");
                row.createCell(5).setCellValue(c.getCreatedAt() != null ? c.getCreatedAt().format(FMT) : "");
            }

            autoSizeColumns(sheet, headers.length);
            return saveFile(wb, "评论列表");
        }
    }

    /**
     * 生成帖子列表 Excel
     */
    public String generateForumPostExcel() throws Exception {
        List<ForumPost> posts = forumPostMapper.selectList(
            new LambdaQueryWrapper<ForumPost>().orderByDesc(ForumPost::getCreatedAt).last("LIMIT 500")
        );

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("帖子列表");
            CellStyle headerStyle = createHeaderStyle(wb);

            String[] headers = {"帖子ID", "用户ID", "内容摘要", "点赞数", "评论数", "状态", "发布时间"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (ForumPost p : posts) {
                Row row = sheet.createRow(rowIdx++);
                String preview = p.getContent() != null && p.getContent().length() > 50
                    ? p.getContent().substring(0, 50) + "..." : p.getContent();
                row.createCell(0).setCellValue(p.getId() != null ? p.getId() : "");
                row.createCell(1).setCellValue(p.getUserId() != null ? p.getUserId() : "");
                row.createCell(2).setCellValue(preview != null ? preview : "");
                row.createCell(3).setCellValue(p.getLikeCount() != null ? p.getLikeCount() : 0);
                row.createCell(4).setCellValue(p.getCommentCount() != null ? p.getCommentCount() : 0);
                row.createCell(5).setCellValue(p.getStatus() != null ? p.getStatus() : "");
                row.createCell(6).setCellValue(p.getCreatedAt() != null ? p.getCreatedAt().format(FMT) : "");
            }

            autoSizeColumns(sheet, headers.length);
            return saveFile(wb, "帖子列表");
        }
    }

    // ---- 私有工具方法 ----

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
            // 防止中文列宽过窄
            int width = sheet.getColumnWidth(i);
            if (width < 3000) sheet.setColumnWidth(i, 3000);
        }
    }

    private String saveFile(XSSFWorkbook wb, String name) throws Exception {
        String dir = uploadPath + "/exports";
        File dirFile = new File(dir);
        if (!dirFile.exists()) dirFile.mkdirs();

        String filename = name + "_" + LocalDateTime.now().format(FILE_FMT) + ".xlsx";
        String filePath = dir + "/" + filename;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            wb.write(fos);
        }

        log.info("Excel 生成成功：{}", filePath);
        return baseUrl + "/exports/" + filename;
    }
}
