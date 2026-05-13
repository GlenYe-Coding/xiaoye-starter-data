package com.xiaoye.starter.data.crud;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 通用CRUD服务实现类
 *
 * @param <M> Mapper类型
 * @param <T> 实体类型
 */
public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements BaseService<M, T> {

    @Override
    public boolean saveBatch(Collection<T> entityList, int batchSize) {
        String sqlStatement = SqlHelper.getSqlStatement(this.getClass(), SqlHelper::saveBatch);
        return executeBatch(entityList, batchSize, (sqlSession, entity) -> sqlSession.insert(sqlStatement, entity));
    }

    @Override
    public boolean updateBatchById(Collection<T> entityList, int batchSize) {
        String sqlStatement = SqlHelper.getSqlStatement(this.getClass(), SqlHelper::updateBatchById);
        return executeBatch(entityList, batchSize, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
    }

    /**
     * 执行批量操作
     */
    protected <E> boolean executeBatch(Collection<E> entityList, int batchSize,
                                      java.util.function.BiConsumer<org.apache.ibatis.session.SqlSession, E> consumer) {
        return SqlHelper.executeBatch(this.getClass(), this.getBaseMapper(), entityList, batchSize, consumer);
    }
}