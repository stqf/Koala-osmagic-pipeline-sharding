import com.osmagic.pipeline.sharding.utils.CommUtils

def build(String typeItem, String project, Map item) {
    def tarItem
    def regexItem = item.get("regexItem")
    if ("Java".equals(typeItem)) {
        tarItem = sh(script: "find ./ -name $regexItem | sort -r | head -n 1", returnStdout: true).trim()
    } else if ("Web".equals(typeItem)) {
        tarItem = sh(script: "find ./ -name $regexItem | sort | head -n 1", returnStdout: true).trim()
    } else {
        tarItem = ""
        error "不能支持的类型[$typeItem], 请联系管理员 ... 项目:$project"
    }

    println("Build[$project] finish ... tarItem：$tarItem")
}

def builds(List projects) {
    def tasks = [:]
    projects.each {

        def nameItem = it.get("project")
        String typeItem = it.get("type")

        def requireItem = CommUtils.isRequireHandler(nameItem, params)
        println("Build $nameItem ... requireItem: $requireItem ")

        if (!requireItem) {
            return
        }

        List<Map> deployItems = it.get("deploy")

        for (Map item : deployItems) {
            def suffixItem = item.get("name")
            def kItem = suffixItem == null ? nameItem : "$nameItem-$suffixItem"
            def requireIt = CommUtils.isRequireHandler(kItem, params)
            if (!requireIt) {
                return
            }

            tasks."Project[$nameItem]" = {
                build(typeItem, nameItem, item)
                echo "Project[$nameItem] Build finish ..."
            }
        }

    }

    parallel tasks
}