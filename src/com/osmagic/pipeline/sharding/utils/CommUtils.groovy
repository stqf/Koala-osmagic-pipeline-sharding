package com.osmagic.pipeline.sharding.utils

class CommUtils {

    /**
     * 创建StringBuilder
     * @return
     */
    static StringBuilder createSbItem() {
        return new StringBuilder();
    }

    /**
     * 是否需要处理
     * @param name
     * @param params
     * @return
     */
    static boolean isRequireHandler(String nameItem, Map params) {

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

        return requireItem;
    }

    /**
     * 需要处理的项目
     * @param items
     * @param params
     * @return
     */
    static List<Map> requireHandlerProjects(List<Map> items, Map params) {
        List<Map> reItems = new ArrayList<>()
        for (Map item : items) {
            String nameItem = item.get("project")
            if (isRequireHandler(nameItem, params)) {
                reItems.add(item)
            }
        }

        for (Map reItem : reItems) {
            String nameItem = reItem.get("project")
            List<Map> deployItems = reItem.get("deploy")
            List<Map> requireItems = new ArrayList<>()
            for (Map item : deployItems) {

                def suffixItem = item.get("name")
                def kItem = suffixItem == null ? nameItem : "$nameItem-$suffixItem"
                def requireIt = isRequireHandler(kItem, params)
                if (requireIt) {
                    requireItems.add(item)
                }
            }
            reItem.put("deploy", requireItems)
        }

        return reItems
    }

}
