package com.example.fintechpagoflex.util

data class PaymentValidationResult(
    val isMethodValid: Boolean,
    val methodError: String?,
    val isConfirmedValid: Boolean,
    val confirmationError: String?,
    val isValid: Boolean
)

data class ReminderValidationResult(
    val isDateTimeValid: Boolean,
    val dateTimeError: String?,
    val isMessageValid: Boolean,
    val messageError: String?,
    val isValid: Boolean
)

data class RecommendValidationResult(
    val isOrgNameValid: Boolean,
    val orgNameError: String?,
    val isOrgTypeValid: Boolean,
    val orgTypeError: String?,
    val isValid: Boolean
)

fun validatePaymentForm(method: String?, confirmed: Boolean): PaymentValidationResult {
    val isMethodValid = !method.isNullOrBlank()
    val methodError = if (!isMethodValid) "Debes seleccionar un método de pago." else null
    val isConfirmedValid = confirmed
    val confirmationError = if (!isConfirmedValid) "Debes confirmar que esto es una simulación." else null
    return PaymentValidationResult(
        isMethodValid = isMethodValid,
        methodError = methodError,
        isConfirmedValid = isConfirmedValid,
        confirmationError = confirmationError,
        isValid = isMethodValid && isConfirmedValid
    )
}

fun validateReminderForm(dateTimeMillis: Long, message: String): ReminderValidationResult {
    val isDateTimeValid = dateTimeMillis > System.currentTimeMillis()
    val dateTimeError = if (!isDateTimeValid) "La fecha y hora deben ser en el futuro." else null
    val isMessageValid = message.isNotBlank()
    val messageError = if (!isMessageValid) "El mensaje no puede estar vacío." else null
    return ReminderValidationResult(
        isDateTimeValid = isDateTimeValid,
        dateTimeError = dateTimeError,
        isMessageValid = isMessageValid,
        messageError = messageError,
        isValid = isDateTimeValid && isMessageValid
    )
}

fun validateRecommendForm(orgName: String, orgType: String): RecommendValidationResult {
    val isOrgNameValid = orgName.isNotBlank()
    val orgNameError = if (!isOrgNameValid) "El nombre de la organización es obligatorio." else null
    val isOrgTypeValid = orgType.isNotBlank()
    val orgTypeError = if (!isOrgTypeValid) "El tipo de organización es obligatorio." else null
    return RecommendValidationResult(
        isOrgNameValid = isOrgNameValid,
        orgNameError = orgNameError,
        isOrgTypeValid = isOrgTypeValid,
        orgTypeError = orgTypeError,
        isValid = isOrgNameValid && isOrgTypeValid
    )
}

fun validateProfileForm(name: String): String? {
    if (name.isBlank()) {
        return "El nombre no puede estar vacío."
    }
    if (name.length > 40) {
        return "El nombre no puede superar los 40 caracteres."
    }
    return null
}