package com.xiaoye.starter.data.crud;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.Collection;
import java.util.List;

/**
 * 通用CRUD服务接口
 * <p>
 * P1 功能：
 * - 扩展 IService，提供更便捷的增删改查方法
 * - 支持批量操作
 * </p>
 *
 * @param <M> Mapper类型
 * @param <T> 实体类型
 */
public interface BaseService<M extends BaseMapper<T>, T> extends IService<T> {

    /**
     * 根据ID查询
     */
    default T getById(Long id) {
        return getBaseMapper().selectById(id);
    }

    /**
     * 根据ID列表查询
     */
    default List<T> getByIds(Collection<?> ids) {
        return getBaseMapper().selectBatchIds(ids);
    }

    /**
     * 根据单条件查询
     */
    default T getOne(QueryWrapper<T> wrapper) {
        return getBaseMapper().selectOne(wrapper);
    }

    /**
     * 根据条件查询列表
     */
    default List<T> list(QueryWrapper<T> wrapper) {
        return getBaseMapper().selectList(wrapper);
    }

    /**
     * 分页查询
     */
    default IPage<T> page(IPage<T> page, QueryWrapper<T> wrapper) {
        return getBaseMapper().selectPage(page, wrapper);
    }

    /**
     * 统计数量
     */
    default long count(QueryWrapper<T> wrapper) {
        return getBaseMapper().selectCount(wrapper);
    }

    /**
     * 批量插入（MySQL推荐）
     */
    default boolean saveBatch(Collection<T> entityList) {
        return saveBatch(entityList, 1000);
    }

    /**
     * 批量插入（指定批次大小）
     */
    boolean saveBatch(Collection<T> entityList, int batchSize);

    /**
     * 批量更新
     */
    default boolean updateBatchById(Collection<T> entityList) {
        return updateBatchById(entityList, 1000);
    }

    /**
     * 批量更新（指定批次大小）
     */
    boolean updateBatchById(Collection<T> entityList, int batchSize);

    /**
     * 存在性检查
     */
    default boolean exists(QueryWrapper<T> wrapper) {
        return count(wrapper) > 0;
    }

    /**
     * 逻辑删除（如果配置了逻辑删除）
     */
    default boolean removeByIdLogic(Long id) {
        return getBaseMapper().deleteById(id) > 0;
    }
}