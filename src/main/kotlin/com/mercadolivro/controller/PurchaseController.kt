package com.mercadolivro.controller

import com.mercadolivro.controller.mapper.PurchaseMapper
import com.mercadolivro.controller.request.PostPurchaseRequest
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.model.PurchaseModel
import com.mercadolivro.service.PurchaseService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("purchases")
class PurchaseController(
    private val purchaseService: PurchaseService,
    private val purchaseMapper: PurchaseMapper
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun purchase(@RequestBody @Valid request: PostPurchaseRequest){
        purchaseService.create(purchaseMapper.toModel(request))
    }

    @GetMapping
    fun getAllPurchase(): List<PurchaseModel> {
        return purchaseService.getPurchase()
    }

    @GetMapping("/{id}")
    fun getPurchaseAutor(@PathVariable autor_id: Int): PurchaseModel {
        //não está funcionando.
        return purchaseService.getPurchaseAutor(autor_id).get(autor_id)
    }

}