import com.osmagic.pipeline.sharding.utils.CommUtils

def deploy(String project, String pod) {
    println("Deploy[$project] finish ... podItem：$pod")
}


def deploys(List projects) {
    def tasks = [:]
    projects.each {

        def nameItem = it.get("project")
        def requireItem = CommUtils.isRequireHandler(nameItem, params)
        println("Deploy $nameItem ... requireItem: $requireItem ")

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

            List<String> resourceItems = item.get("resources");
            println("resourceItems ... $resourceItems")
            for (String resourceItem : resourceItems) {
                tasks."Project[$nameItem::$resourceItem]" = {
                    deploy(nameItem, resourceItem)
                    echo "Project[$nameItem::$resourceItem] Deploy finish ..."
                }
            }

        }

    }

    parallel tasks
}