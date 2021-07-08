import com.osmagic.pipeline.sharding.utils.CommUtils

def compile(String project) {
    def ts = System.currentTimeMillis()
    String path = "${env.WORKSPACE}/$project"
    def file = new File(path)
    def items = file.list()
    println("[TRACE] handler start : name is $project ")
    if (items.contains("pom.xml")) {
        /*Maven项目编译*/
        def shItem = "cd $project && pwd && mvn package -T 3C"
        println("[DEBUG] compile Java: use type is Maven, name is $project, command is '$shItem'")
        sh "$shItem"
    } else if (items.contains("build.gradle")) {
        /*Gradle项目编译*/
        def reItem = sh(script: "which gradle", returnStatus: true)
        def bashItem = reItem == 0 ? "gradle" : "bash /var/jenkins_home/gradle-6.8.3/bin/gradle"
        def shItem = "cd $project && pwd && $bashItem build -x test --parallel"
        println("[DEBUG] compile Java: use type is Gradle, name is $project, command is '$shItem'")
        sh "$shItem"
    } else if (items.contains("package.json")) {
        def wctItem = Optional.ofNullable(env.WebCompileType).orElse("npm")
        def wctPrepareItem = Optional.ofNullable(env.WebCompilePrepare).orElse("true")
        def cnpmShItem = "true" == wctPrepareItem && !items.contains("node_modules") ? " && cnpm install" : ""
        def shItem = "cd $project $cnpmShItem && $wctItem install && $wctItem run build"
        /*Web项目编译*/
        println("[DEBUG] compile Web : use type is $wctItem, name is $project, command is '$shItem'")
        sh "$shItem"
    } else {
        println("[ERROR] compile Unknown: use type is Unknown, name is $project")
        error "不能识别的项目[$project], 请联系管理员 ... "
    }
    println("[DEBUG] handler finish: name is $project, time is ${System.currentTimeMillis() - ts}ms")
}

def compiles(List projects) {
    def tasks = [:]
    projects.each {

        def nameItem = it.get("project")
        def requireItem = CommUtils.isRequireHandler(nameItem, params)
        println("[TRACE] Compile project start : name is $nameItem, require is $requireItem ")

        /*判断项目是否需要编译*/
        if (!requireItem) {
            return
        }

        tasks."Project[$nameItem]" = {
            compile(nameItem)
            println("[INFO ] Compile project finish: name is $nameItem ")
        }
    }

    parallel tasks
}