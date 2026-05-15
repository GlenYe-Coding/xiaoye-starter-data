package com.xiaoye.starter.data.crud;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * Generic CRUD service implementation
 *
 * @param <M> Mapper type
 * @param <T> Entity type
 */
public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements BaseService<M, T> {

}