import com.osmagic.pipeline.sharding.utils.CommUtils

import java.util.concurrent.TimeUnit

def deployItem(String project, String image, String resourceItem, String podType) {
    String ipItem = params.get("ServerIp")
    String psItem = params.get("ServerPs")
    String podItem = resourceItem.substring(resourceItem.indexOf("-") + 1)
    String sshItem = "kubectl set image $podType/$resourceItem $podItem=$image"
    TimeUnit.MILLISECONDS.sleep(new Random().nextInt(1000))
    def shItem = "sshpass -p $psItem ssh root@$ipItem  -o StrictHostKeyChecking=no \"$sshItem\" || true"
    sh "$shItem"
    println("[DEBUG] deploy pod finish: name is $project, pod is $resourceItem, image is $image, command is '$shItem'")
}


def deploys(List projects, String currentTag) {
    def tasks = [:]
    projects.each {

        def nameItem = it.get("project")
        def requireItem = CommUtils.isRequireHandler(nameItem, params)
        println("[TRACE] Deploy project start : name is $nameItem, require is $requireItem ")

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
                    println("[INFO ] Deploy project finish: name is $nameItem, image is $imageItem, pod is $finalItem, type is $podTypeSwapItem")
                }
            }

        }

    }

    parallel tasks
}