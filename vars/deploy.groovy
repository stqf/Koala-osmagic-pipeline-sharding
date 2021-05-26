import com.osmagic.pipeline.sharding.utils.CommUtils

import java.util.concurrent.TimeUnit

def deployItem(String project, String image, String resourceItem, String podType) {
    String ipItem = params.get("ServerIp")
    String psItem = params.get("ServerPs")
    String podItem = resourceItem.substring(resourceItem.indexOf("-") + 1)
    String sshItem = "kubectl set image $podType/$resourceItem $podItem=$image"
    TimeUnit.MILLISECONDS.sleep(new Random().nextInt(1000))
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

        /*判断项目是否需要发布*/
        if (!requireItem) {
            return
        }

        List<Map> deployItems = it.get("deploy")

        for (Map item : deployItems) {
            def suffixItem = item.get("name")
            def kItem = suffixItem == null ? nameItem : "$nameItem-$suffixItem"
            def requireIt = CommUtils.isRequireHandler(kItem, params)
            /*判断项目下某一模块是否需要发布, 主要用于Java多模块项目*/
            if (!requireIt) {
                return
            }

            String imageName = item.get("image")
            String imageItem = "$imageName:$currentTag"
            List<String> resourceItems = item.get("resources");
            String podType = Optional.ofNullable(item.get("podType")).orElse("deployment")

            for (String finalItem : resourceItems) {
                def swapItem = finalItem
                def podTypeSwapItem = podType
                tasks."Project[$nameItem-$finalItem]Deploy" = {
                    deployItem(nameItem, imageItem, swapItem, podTypeSwapItem)
                    echo "Project[$nameItem-$finalItem] Deploy finish ..."
                }
            }

        }

    }

    parallel tasks
}