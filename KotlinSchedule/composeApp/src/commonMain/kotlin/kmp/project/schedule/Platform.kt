package kmp.project.schedule

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform