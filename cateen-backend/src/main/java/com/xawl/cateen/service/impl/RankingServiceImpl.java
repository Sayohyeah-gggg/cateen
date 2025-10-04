package com.xawl.cateen.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.common.ResultCode;
import com.xawl.cateen.constant.StatusConstants;
import com.xawl.cateen.dto.RankingDTO;
import com.xawl.cateen.entity.Ranking;
import com.xawl.cateen.entity.RankingFood;
import com.xawl.cateen.exception.BusinessException;
import com.xawl.cateen.mapper.RankingFoodMapper;
import com.xawl.cateen.mapper.RankingMapper;
import com.xawl.cateen.service.RankingService;
import com.xawl.cateen.util.UserContext;
import com.xawl.cateen.vo.RankingVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 榜单服务实现类
 *
 * @author xawl
 * @date 2025-10-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final RankingMapper rankingMapper;
    private final RankingFoodMapper rankingFoodMapper;

    @Override
    public Page<RankingVO> getRankingPage(Long pageNum, Long pageSize, String keyword, String type, String status) {
        Page<RankingVO> page = new Page<>(pageNum, pageSize);
        rankingMapper.selectRankingPage(page, keyword, type, status);
        return page;
    }

    @Override
    public RankingVO getRankingDetail(String id) {
        RankingVO rankingVO = rankingMapper.selectRankingDetailById(id);
        if (rankingVO == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "榜单不存在");
        }
        return rankingVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RankingVO createRanking(RankingDTO dto) {
        // 创建榜单
        Ranking ranking = new Ranking();
        ranking.setId(IdUtil.getSnowflakeNextIdStr());
        ranking.setTitle(dto.getTitle());
        ranking.setDescription(dto.getDescription());
        ranking.setType(StrUtil.isNotBlank(dto.getType()) ? dto.getType() : "general");
        ranking.setStatus(StatusConstants.ACTIVE);
        ranking.setCreatedBy(UserContext.getUserId());

        rankingMapper.insert(ranking);

        // 创建榜单美食关联
        if (CollUtil.isNotEmpty(dto.getFoodIds())) {
            List<RankingFood> relations = IntStream.range(0, dto.getFoodIds().size())
                    .mapToObj(index -> {
                        RankingFood relation = new RankingFood();
                        relation.setId(IdUtil.getSnowflakeNextIdStr());
                        relation.setRankingId(ranking.getId());
                        relation.setFoodId(dto.getFoodIds().get(index));
                        relation.setRankPosition(index + 1);
                        relation.setCreatedAt(LocalDateTime.now());
                        return relation;
                    })
                    .collect(Collectors.toList());
            rankingFoodMapper.batchInsert(relations);
        }

        return getRankingDetail(ranking.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRanking(String id, RankingDTO dto) {
        Ranking ranking = rankingMapper.selectById(id);
        if (ranking == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "榜单不存在");
        }

        // 更新榜单信息
        ranking.setTitle(dto.getTitle());
        ranking.setDescription(dto.getDescription());
        if (StrUtil.isNotBlank(dto.getType())) {
            ranking.setType(dto.getType());
        }

        rankingMapper.updateById(ranking);

        // 更新榜单美食关联
        if (dto.getFoodIds() != null) {
            // 删除旧的关联
            rankingFoodMapper.deleteByRankingId(id);

            // 创建新的关联
            if (CollUtil.isNotEmpty(dto.getFoodIds())) {
                List<RankingFood> relations = IntStream.range(0, dto.getFoodIds().size())
                        .mapToObj(index -> {
                            RankingFood relation = new RankingFood();
                            relation.setId(IdUtil.getSnowflakeNextIdStr());
                            relation.setRankingId(id);
                            relation.setFoodId(dto.getFoodIds().get(index));
                            relation.setRankPosition(index + 1);
                            relation.setCreatedAt(LocalDateTime.now());
                            return relation;
                        })
                        .collect(Collectors.toList());
                rankingFoodMapper.batchInsert(relations);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRanking(String id) {
        Ranking ranking = rankingMapper.selectById(id);
        if (ranking == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "榜单不存在");
        }

        rankingMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRankingStatus(String id, String status) {
        Ranking ranking = rankingMapper.selectById(id);
        if (ranking == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "榜单不存在");
        }

        ranking.setStatus(status);
        rankingMapper.updateById(ranking);
    }

}

