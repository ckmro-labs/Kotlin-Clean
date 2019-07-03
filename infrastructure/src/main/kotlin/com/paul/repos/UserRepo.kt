package com.paul.repos

import com.paul.entity.UserDataClass
import com.paul.models.User
import com.paul.port.UserDoesNotExistException
import com.paul.ports.UserPorts
import com.paul.validators.UserValidator
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepo: BaseRepo() {

    fun create(user: UserDataClass){
        val validator = UserValidator(user)
        validator.validateCreateUser()

        transaction {
            User.insert {
                it[email] = user.email
                it[firstName] = user.firstName
                it[lastName] = user.lastName
                it[password] = UserPorts.hashPassword( user.password )
            }
        }
    }

    fun findUserByEmail(email: String, password: String): HashMap<String, String>? {
        /*
        * returns a single users information as Map
        * format ("id"=user.id, "email"=user.email, "firstName"=user.firstName, "lastName"=user.lastName)
         */

        val validator = UserValidator(UserDataClass(email=email))
        val user: HashMap<String, String>  = HashMap()

        validator.validateEmail()


        transaction {
            User.select {
                User.email eq email
                User.password eq password
            }.map {
                user["id"] = it[User.id].toString()
                user["email"] = it[User.email]
                user["firstName"] = it[User.firstName]
                user["lastName"] = it[User.lastName]
            }
        }

        if (user.isEmpty())
            throw UserDoesNotExistException("email and password could no be validated")

        return user
    }

    fun findAllUsers(): HashMap<String, HashMap<String, String>> {
        /*
        * returns a HashMap with the each users' details
        * In the format:
        * Hash<"userId", Hash< ("id"=user.id, "email"=user.email, "firstName"=user.firstName, "lastName"=user.lastName)>>
        * returns an empty hashmap if no user is found
         */

        val users: HashMap<String, HashMap<String, String>> = HashMap()

        transaction {
            User.selectAll().forEach {
                val userArray: HashMap<String, String> = HashMap()
                userArray["id"] = it[User.id].toString()
                userArray["email"] = it[User.email]
                userArray["firstName"] = it[User.firstName]
                userArray["lastName"] = it[User.lastName]
                users[it[User.id].toString()] =userArray
            }
        }
        return users
    }

    fun findUserById(id: Long): HashMap<String, String> {
        /*
        *
         */
        val userInfo :HashMap<String, String> = HashMap()

        transaction {
           User.select {
               User.id eq id
           }.withDistinct().map {
               userInfo["id"] = it[User.id].toString()
               userInfo["email"] = it[User.email]
               userInfo["firstName"] = it[User.firstName]
               userInfo["lastName"] = it[User.lastName]
           }
        }

        if (userInfo.isEmpty())
            throw UserDoesNotExistException("user could not be found")

        return userInfo
    }
}