package com.example.security.domain

import org.springframework.data.jpa.repository.JpaRepository

interface RedeemCodeRepository: JpaRepository<RedeemCode, Long>
