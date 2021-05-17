import com.osmagic.pipeline.sharding.utils.CommUtils

def deployItem(String project, String image, String resourceItem) {
    String ipItem = params.get("ServerIp")
    String psItem = params.get("ServerPs")
    String podItem = resourceItem.substring(resourceItem.indexOf("-") + 1)
    String sshItem = "kubectl set image deployment/$resourceItem $podItem=$image"
    sh """
        sshpass -p $psItem ssh root@$ipItem  -o StrictHostKeyChecking=no "$sshItem" || true
    """
    println("Deploy[$project] finish ... podItem：$resourceItem image:$image sshItem:$sshItem")
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

            for (String finalItem : resourceItems) {
                def swapItem = finalItem
                tasks."Project[$nameItem-$finalItem]Deploy" = {
                    deployItem(nameItem, imageItem, swapItem)
                    echo "Project[$nameItem-$finalItem] Deploy finish ..."
                }
            }

        }

    }

    parallel tasks
}