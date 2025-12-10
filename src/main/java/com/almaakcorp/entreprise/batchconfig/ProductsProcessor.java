package com.almaakcorp.entreprise.batchconfig;

import com.almaakcorp.entreprise.models.Products;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class ProductsProcessor implements ItemProcessor<Products,Products> {


    @Override
    public Products process(Products products) throws Exception {
        return products;
    }
}
