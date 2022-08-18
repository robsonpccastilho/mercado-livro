package com.mercadolivro.service

import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.enums.Errors
import com.mercadolivro.enums.Role
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.helper.buildCustomer
import com.mercadolivro.repositoy.CustomerRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*


@ExtendWith(MockKExtension::class)
class CustomerServiceTest {

    @MockK
    lateinit var customerRepository: CustomerRepository

    @MockK
    lateinit var bookService : BookService

    @MockK
    lateinit var bCrypt: BCryptPasswordEncoder

    @InjectMockKs
    @SpyK
    private lateinit var customerService: CustomerService

    @Test
    fun `should return all customers`(){

        //val customer1 = buildCustomer()
        val fakeCustomers = listOf(buildCustomer(),buildCustomer())
        every { customerRepository.findAll() } returns fakeCustomers
        val customers = customerService.getAll(null)

        assertEquals(fakeCustomers, customers)
        //verifica que foi chamado exatamente uma vez, se for mais ele quebra
        verify ( exactly = 1 ) {customerRepository.findAll()}
        //verifica que foi não foi chamado nenhuma vez com nome
        verify ( exactly = 0 ) {customerRepository.findByNameContaining(any())}

    }

    @Test
    fun `should return customers when name is informed`(){
        //val name = "Robson"
        //val name = Math.random().toString() //gera uma string aleatória
        val name = UUID.randomUUID().toString() //gera uma string aleatória
        //val customer1 = buildCustomer()
        val fakeCustomers = listOf(buildCustomer(),buildCustomer())

        every { customerRepository.findByNameContaining(name) } returns fakeCustomers

        val customers = customerService.getAll(name)

        assertEquals(fakeCustomers, customers)
        //verifica que foi chamado exatamente uma vez, se for mais ele quebra
        verify ( exactly = 0 ) {customerRepository.findAll()}
        //verifica que foi não foi chamado nenhuma vez com nome
        //verify ( exactly = 1 ) {customerRepository.findByNameContaining(any())}
        verify ( exactly = 1 ) {customerRepository.findByNameContaining(name)}

    }


    @Test
    fun `should create customer and encrypt password`(){
        val inicialPassord = Math.random().toString()
        //val fakeCustomer = buildCustomer()
        val fakeCustomer = buildCustomer(password = inicialPassord)
        val fakePassword = UUID.randomUUID().toString()
        val fakeCustomerEncrypted = fakeCustomer.copy(password = fakePassword)

        //every { customerRepository.save(any()) } returns fakeCustomer
        every { customerRepository.save(fakeCustomerEncrypted) } returns fakeCustomer
        //every { bCrypt.encode(any()) } returns fakePassword
        every { bCrypt.encode(inicialPassord) } returns fakePassword

        customerService.createCustomer(fakeCustomer)

        //verify (exactly = 1) { customerRepository.save(any())}
        verify (exactly = 1) { customerRepository.save(fakeCustomerEncrypted)}
        //verify (exactly = 1) { bCrypt.encode(any())}
        verify (exactly = 1) { bCrypt.encode(inicialPassord)}

    }


    @Test
    fun `fun test`(){
        val resultado = soma(2,3)
        //dá erro pois o experado é 6 e o resultado deu 5
        //assertEquals(6, resultado)
        //dá certo pois o resultado esperado é 5 e dá 5
        assertEquals(5, resultado)
    }

    private fun soma(a: Int, b:Int): Int{
        return a + b
    }

    @Test
    fun `should return customer by id`(){
        val id = Random().nextInt()
        val fakeCustomer = buildCustomer(id = id)
        every { customerRepository.findById(id) } returns Optional.of(fakeCustomer)

        val customer = customerService.findById(id)

        assertEquals(fakeCustomer, customer)

        verify(exactly = 1) { customerRepository.findById(id) }

    }

    @Test
    fun `should throw erro when find by and customer not found`(){
        val id = Random().nextInt()

        every { customerRepository.findById(id) } returns Optional.empty()

        val error = assertThrows<NotFoundException> { customerService.findById(id)}

        assertEquals("Customer [${id}] not exists!!!", error.message)
        assertEquals("ML-201", error.errorCode)

        verify(exactly = 1) { customerRepository.findById(id) }

    }

    @Test
    fun `should update customer`(){
        val id = Random().nextInt()
        val fakeCustomer = buildCustomer(id = id)

        every { customerRepository.existsById(id) } returns true
        every { customerRepository.save(fakeCustomer) } returns fakeCustomer

        customerService.updateCustomer(fakeCustomer)

        verify(exactly = 1) { customerRepository.existsById(id) }
        verify(exactly = 1) { customerRepository.save(fakeCustomer) }

    }

    @Test
    fun `should throw not found exception when update customer`(){
        val id = Random().nextInt()
        val fakeCustomer = buildCustomer(id = id)
        /**every moca os serviços */
        every { customerRepository.existsById(id) } returns false
        every { customerRepository.save(fakeCustomer) } returns fakeCustomer

        val error = assertThrows<NotFoundException> { customerService.updateCustomer(fakeCustomer)}

        assertEquals("Customer [${id}] not exists!!!", error.message)
        assertEquals("ML-201", error.errorCode)

        verify(exactly = 1) { customerRepository.existsById(id) }
        verify(exactly = 0) { customerRepository.save(any()) }

    }

    @Test
    fun `should delete customer`(){
        val id = Random().nextInt()
        val fakeCustomer = buildCustomer(id = id)
        val expectedCustomer = fakeCustomer.copy(status = CustomerStatus.INATIVO)
        /**every moca os serviços */
        every { customerService.findById(id) } returns fakeCustomer
        every { customerRepository.save(expectedCustomer) } returns expectedCustomer
        every { bookService.deleteByCustomer(fakeCustomer) } just runs

        customerService.deleteCustomer(id)

        verify(exactly = 1) { customerService.findById(id) }
        verify(exactly = 1) { bookService.deleteByCustomer(fakeCustomer) }
        verify(exactly = 1) { customerRepository.save(expectedCustomer) }


    }

    @Test
    fun `should throw not found exception when delete customer`(){
        val id = Random().nextInt()

        /**every moca os serviços */
        every { customerService.findById(id) }  throws NotFoundException(Errors.ML201.message.format(id), Errors.ML201.code)

        val error = assertThrows<NotFoundException> { customerService.deleteCustomer(id)}

        assertEquals("Customer [${id}] not exists!!!", error.message)
        assertEquals("ML-201", error.errorCode)

        verify(exactly = 1) { customerService.findById(id) }
        verify(exactly = 0) { bookService.deleteByCustomer(any()) }
        verify(exactly = 0) { customerRepository.save(any()) }

    }

    @Test
    fun `should return true when email available`(){
        val email = "${Random().nextInt().toString()}@email.com"

        every { customerRepository.existsByEmail(email) } returns false

        val emailAvailable =  customerService.emailAvailable(email)

        assertTrue(emailAvailable)
        verify( exactly =  1) { customerRepository.existsByEmail(email) }

    }

    @Test
    fun `should return false when email unavailable`(){
        val email = "${Random().nextInt().toString()}@email.com"

        every { customerRepository.existsByEmail(email) } returns true

        val emailAvailable =  customerService.emailAvailable(email)

        assertFalse(emailAvailable)
        verify( exactly =  1) { customerRepository.existsByEmail(email) }

    }


}
