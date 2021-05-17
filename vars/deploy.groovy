import com.osmagic.pipeline.sharding.utils.CommUtils

def deploy(String project, String image, String pod) {
    String ipItem = params.get("ServerIp")
    String psItem = params.get("ServerPs")
    String podItem = pod.substring(pod.indexOf("-") + 1)
    String sshItem = "kubectl set image deployment/$pod $podItem=$image"
    sh """
        sshpass -p $psItem ssh root@$ipItem  -o StrictHostKeyChecking=no "$sshItem" || true
    """
    println("Deploy[$project] finish ... podItem：$pod image:$image sshItem:$sshItem")
}


def deploys(List projects, String currentTag) {
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
            String imageName = item.get("image")
            String imageItem = "$imageName:$currentTag"
            List<String> resourceItems = item.get("resources");
            println("resourceItems ... $resourceItems")
            for (String resourceItem : resourceItems) {
                tasks."Project[$nameItem-$resourceItem]" = {
                    deploy(nameItem, imageItem, resourceItem)
                    echo "Project[$nameItem::$resourceItem] Deploy finish ..."
                }
            }

        }

    }

    parallel tasks
}