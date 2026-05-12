package com.xiaoye.starter.data.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoye.starter.common.base.BaseEntity;

import java.io.Serializable;
import java.util.List;

/**
 * 通用CRUD服务基类
 * 封装常用的数据库操作方法
 * 
 * @param <M> Mapper类型
 * @param <T> 实体类型
 */
public class BaseCrudService<M extends BaseMapper<T>, T extends BaseEntity> extends ServiceImpl<M, T> {

    /**
     * 根据ID查询（排除逻辑删除）
     */
    public T getByIdExcludeDeleted(Serializable id) {
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseEntity::getId, id);
        wrapper.eq(BaseEntity::getDeleted, 0);
        return this.getOne(wrapper);
    }

    /**
     * 逻辑删除（软删除）
     */
    public boolean logicDeleteById(Serializable id) {
        T entity = this.getById(id);
        if (entity != null) {
            entity.setDeleted(1);
            return this.updateById(entity);
        }
        return false;
    }

    /**
     * 批量逻辑删除
     */
    public boolean logicDeleteBatchIds(List<? extends Serializable> idList) {
        if (idList == null || idList.isEmpty()) {
            return false;
        }
        
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BaseEntity::getId, idList);
        
        T updateEntity = this.getEntityClass().newInstance();
        try {
            updateEntity.setDeleted(1);
        } catch (Exception e) {
            throw new RuntimeException("设置删除标识失败", e);
        }
        
        return this.update(updateEntity, wrapper);
    }

    /**
     * 恢复逻辑删除
     */
    public boolean restoreDeleted(Serializable id) {
        T entity = this.getById(id);
        if (entity != null) {
            entity.setDeleted(0);
            return this.updateById(entity);
        }
        return false;
    }

    /**
     * 查询所有未删除的记录
     */
    public List<T> listNotDeleted() {
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseEntity::getDeleted, 0);
        return this.list(wrapper);
    }
}
