package com.example.userlistapp.data.dto

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName

@Serializable
data class UserResponseDTO(
    @SerialName("limit")
    val limit: Int?,
    @SerialName("skip")
    val skip: Int?,
    @SerialName("total")
    val total: Int?,
    @SerialName("users")
    val users: List<UserDTO>?
)

@Serializable
data class UserDTO(
    @SerialName("address")
    val address: Address?,
    @SerialName("age")
    val age: Int?,
    @SerialName("bank")
    val bank: Bank?,
    @SerialName("birthDate")
    val birthDate: String?,
    @SerialName("bloodGroup")
    val bloodGroup: String?,
    @SerialName("company")
    val company: Company?,
    @SerialName("crypto")
    val crypto: Crypto?,
    @SerialName("ein")
    val ein: String?,
    @SerialName("email")
    val email: String?,
    @SerialName("eyeColor")
    val eyeColor: String?,
    @SerialName("firstName")
    val firstName: String?,
    @SerialName("gender")
    val gender: String?,
    @SerialName("hair")
    val hair: Hair?,
    @SerialName("height")
    val height: Double?,
    @SerialName("id")
    val id: Int,
    @SerialName("image")
    val image: String?,
    @SerialName("ip")
    val ip: String?,
    @SerialName("lastName")
    val lastName: String?,
    @SerialName("macAddress")
    val macAddress: String?,
    @SerialName("maidenName")
    val maidenName: String?,
    @SerialName("password")
    val password: String?,
    @SerialName("phone")
    val phone: String?,
    @SerialName("role")
    val role: String?,
    @SerialName("ssn")
    val ssn: String?,
    @SerialName("university")
    val university: String?,
    @SerialName("userAgent")
    val userAgent: String?,
    @SerialName("username")
    val username: String?,
    @SerialName("weight")
    val weight: Double?
)

@Serializable
data class Address(
    @SerialName("address")
    val address: String?,
    @SerialName("city")
    val city: String?,
    @SerialName("coordinates")
    val coordinates: Coordinates?,
    @SerialName("country")
    val country: String?,
    @SerialName("postalCode")
    val postalCode: String?,
    @SerialName("state")
    val state: String?,
    @SerialName("stateCode")
    val stateCode: String?
)

@Serializable
data class Bank(
    @SerialName("cardExpire")
    val cardExpire: String?,
    @SerialName("cardNumber")
    val cardNumber: String?,
    @SerialName("cardType")
    val cardType: String?,
    @SerialName("currency")
    val currency: String?,
    @SerialName("iban")
    val iban: String?
)

@Serializable
data class Company(
    @SerialName("address")
    val address: Address?,
    @SerialName("department")
    val department: String?,
    @SerialName("name")
    val name: String?,
    @SerialName("title")
    val title: String?
)

@Serializable
data class Crypto(
    @SerialName("coin")
    val coin: String?,
    @SerialName("network")
    val network: String?,
    @SerialName("wallet")
    val wallet: String?
)

@Serializable
data class Hair(
    @SerialName("color")
    val color: String?,
    @SerialName("type")
    val type: String?
)

@Serializable
data class Coordinates(
    @SerialName("lat")
    val lat: Double?,
    @SerialName("lng")
    val lng: Double?
)


