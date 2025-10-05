package com.xawl.cateen.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xawl.cateen.entity.SearchHistory;
import com.xawl.cateen.mapper.SearchHistoryMapper;
import com.xawl.cateen.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索历史服务实现类
 *
 * @author xawl
 * @date 2025-10-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private final SearchHistoryMapper searchHistoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordSearch(String userId, String keyword) {
        if (userId == null || keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        keyword = keyword.trim();
        
        // 查找是否已存在该用户的搜索记录
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId)
                .eq(SearchHistory::getKeyword, keyword);
        
        SearchHistory existing = searchHistoryMapper.selectOne(wrapper);
        
        if (existing != null) {
            // 更新现有记录
            existing.setSearchCount(existing.getSearchCount() + 1);
            existing.setLastSearchTime(LocalDateTime.now());
            searchHistoryMapper.updateById(existing);
        } else {
            // 创建新记录
            SearchHistory searchHistory = SearchHistory.builder()
                    .id(IdUtil.simpleUUID())
                    .userId(userId)
                    .keyword(keyword)
                    .searchCount(1)
                    .lastSearchTime(LocalDateTime.now())
                    .build();
            searchHistoryMapper.insert(searchHistory);
        }
        
        log.debug("记录搜索历史，userId: {}, keyword: {}", userId, keyword);
    }

    @Override
    public List<String> getHotKeywords(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return searchHistoryMapper.getHotKeywords(limit);
    }

    @Override
    public List<String> getUserSearchHistory(String userId, Integer limit) {
        if (userId == null) {
            return List.of();
        }
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return searchHistoryMapper.getUserSearchHistory(userId, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearUserSearchHistory(String userId) {
        if (userId == null) {
            return;
        }
        
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId);
        
        int count = searchHistoryMapper.delete(wrapper);
        log.info("清除用户搜索历史，userId: {}, 删除记录数: {}", userId, count);
    }
}
