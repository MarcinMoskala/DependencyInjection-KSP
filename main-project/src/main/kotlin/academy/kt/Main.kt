package academy.kt


@Provide
class UserRepository {
    // ...
}

@Provide
class UserService(
    val userRepository: UserRepository
) {
    // ...
}


fun main() {

}