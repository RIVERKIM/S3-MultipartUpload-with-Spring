package com.example.security.domain

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id


@Entity
class RedeemCode(
    @Id
    @GeneratedValue
    val id: Long? = null,
    @Column
    val redeemCode: String,
    @Column
    val price: Int,
    @Column
    val reason: String
)
