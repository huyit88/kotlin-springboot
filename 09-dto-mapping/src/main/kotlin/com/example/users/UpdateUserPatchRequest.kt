package com.example.users

data class UpdateUserPatchRequest(val name: String?, val email: String?){
    fun isEmpty() = name == null && email == null 
}

fun User.applyPatch(req: UpdateUserPatchRequest) = copy(name = req.name ?: name, email = req.email ?: email)