buildscript {
    repositories {
        google()
        jcenter()
        
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.21")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        
    }

    gradle.projectsEvaluated {
        tasks.withType(JavaCompile::class) {
            options.compilerArgs = options.compilerArgs + "-Xlint:unchecked" + "-Xlint:deprecation"
        }
    }
}


tasks {
    withType(Delete::class.java) {
        delete(rootProject.buildDir)
    }
}
