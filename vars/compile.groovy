def build(String project) {
    String path = "${env.WORKSPACE}/$project"
    def file = new File(path)
    def items = file.list()
    println("Build[$project] start ... ")
    if (items.contains("pom.xml")) {
        println("[$project] Maven ")
        sh """
            cd $project && pwd && mvn package -T 3C 
        """
    } else if (items.contains("build.gradle")) {
        println("[$project] Gradle ")
        def reItem = sh(script: "which gradle", returnStatus: true)
        def bashItem = reItem == 0 ? "gradle" : "bash /var/jenkins_home/gradle-6.8.3/bin/gradle"
        sh """
            cd $project && pwd && $bashItem build -x test --parallel
        """
    } else if (items.contains("package.json")) {
        println("[$project] Web ")
        sh """
            cd $project && pwd && npm install && npm run build
        """
    } else {
        println("[$project] Unknown ")
        error "不能识别的项目[$project], 请联系管理员 ... "
    }
    println("Build[$project] finish ... ")
}

def builds(String[] items) {
    def tasks = [:]
    items.each {
        tasks."Project[$it]" = {
            build(it)
            echo "Project[$it] Bild finish ..."
        }
    }

    parallel tasks
}