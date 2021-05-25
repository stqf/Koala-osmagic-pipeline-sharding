import com.osmagic.pipeline.sharding.utils.CommUtils

def compile(String project) {
    String path = "${env.WORKSPACE}/$project"
    def file = new File(path)
    def items = file.list()
    println("Build[$project] start ... ")
    if (items.contains("pom.xml")) {
        /*Maven项目编译*/
        println("[$project] Maven ")
        sh """
            cd $project && pwd && mvn package -T 3C 
        """
    } else if (items.contains("build.gradle")) {
        /*Gradle项目编译*/
        println("[$project] Gradle ")
        def reItem = sh(script: "which gradle", returnStatus: true)
        def bashItem = reItem == 0 ? "gradle" : "bash /var/jenkins_home/gradle-6.8.3/bin/gradle"
        sh """
            cd $project && pwd && $bashItem build -x test --parallel
        """
    } else if (items.contains("package.json")) {
        /*Web项目编译*/
        println("[$project] Web ")
        sh """
            cd $project && pwd && npm install && npm run build
        """
    } else {
        println("[$project] Unknown ")
        error "不能识别的项目[$project], 请联系管理员 ... "
    }
    println("Compile[$project] finish ... ")
}

def compiles(List projects) {
    def tasks = [:]
    projects.each {

        def nameItem = it.get("project")

        def requireItem = CommUtils.isRequireHandler(nameItem, params)
        println("Compile $nameItem ... requireItem: $requireItem ")

        /*判断项目是否需要编译*/
        if (!requireItem) {
            return
        }

        tasks."Project[$nameItem]" = {
            compile(nameItem)
            echo "Project[$nameItem] Compile finish ..."
        }
    }

    parallel tasks
}