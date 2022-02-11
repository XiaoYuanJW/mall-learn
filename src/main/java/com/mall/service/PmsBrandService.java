package com.mall.service;

import com.mall.mbg.model.PmsBrand;

import java.util.List;

/**
 * PmsBrandService
 */
public interface PmsBrandService {
    /**
     * 查询所有品牌
     * @return
     */
    List<PmsBrand> listAllBrand();

    /**
     * 分页查询品牌
     * @param pageNum
     * @param pageSize
     * @return
     */
    List<PmsBrand> listBrand(int pageNum,int pageSize);

    /**
     * id查询品牌
     * @param id
     * @return
     */
    PmsBrand getBrand(Long id);

    /**
     * 新增品牌
     * @param brand
     * @return
     */
    int createBrand(PmsBrand brand);

    /**
     * 修改品牌
     * @return
     */
    int updateBrand(Long id,PmsBrand brand);

    /**
     * 删除品牌
     * @param id
     * @return
     */
    int deleteBrand(Long id);
}
