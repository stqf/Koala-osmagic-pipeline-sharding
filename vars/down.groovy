def clone(Map project) {
    //println("Clone ... $project ")
    // println("params ... $params")

    String nameItem = project.get("project")
    def requireItem = params.get("000-build-all-application")
    if (!requireItem) {
        def kItems = params.keySet()
        for (String kItem : kItems) {
            def containsItem = kItem.contains(nameItem)
            //println("kItem: $kItem --- nameItem:$nameItem --- contains:$containsItem ")
            if (containsItem && params.get(kItem)) {
                requireItem = true
                break
            }
        }
    }

    println("Clone $nameItem ... $requireItem ")

    if (!requireItem) {
        return
    }

    def addressKey = "$nameItem-address".replaceAll("-", "_")
    def address = env."$addressKey"
    if (!address) {
        error "Project[$nameItem] address is unknown ... $addressKey "
    }

    def branchItem = params.get("Koala-osmaigc-all-barnch")

    sh """
        if [ -d "$nameItem" ]; then
            cd $nameItem && git reset --hard HEAD && git checkout $branchItem && git pull 
            echo "更新代码 ..."
        else
            git clone $address -b $branchItem
            echo "拉取代码 ..."
        fi
    """
}

def clones(List projects) {
    def tasks = [:]
    projects.each {
        def nameItem = it.get("project")
        tasks."Project[$nameItem]" = {
            clone(it)
            echo "Project[$nameItem] Clone finish ..."
        }
    }

    parallel tasks
}