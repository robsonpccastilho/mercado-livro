package com.mercadolivro.service

import com.mercadolivro.events.PurchaseEvent
import com.mercadolivro.model.PurchaseModel
import com.mercadolivro.repositoy.PurchaseRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.util.*

@Service
class PurchaseService(
    private val purchaseRepository: PurchaseRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    fun create(purchaseModel: PurchaseModel){
        purchaseRepository.save(purchaseModel)
        println("Disparando evento de compra!!!")
        applicationEventPublisher.publishEvent(PurchaseEvent(this, purchaseModel))
        println("Finalização do processamento do evento de compra!!!")
    }

    fun update(purchaseModel: PurchaseModel) {
        purchaseRepository.save(purchaseModel)
    }

    fun getPurchase(): List<PurchaseModel>{
        return purchaseRepository.findAll().toList()
    }

    fun getPurchaseAutor(autor_id: Int): List<PurchaseModel>{
        return purchaseRepository.findAll().toList()
    }

/*
    fun booksAvaliable(value: Int): Boolean {
        return true
    }
*/

}
