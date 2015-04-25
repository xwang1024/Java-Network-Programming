缓冲区
==========
总述
----------
#流和通道的区别在于前者基于字节而后者基于块，NIO使用了通道作为传输手段而Buffer则作为块的构造方式。Java中，每种基本类型都对应着一种Buffer。
#对于任何一种Buffer，都有4个部分的关键信息：
##位置（Position）：索引记录将要写入或读取的下一个位置
##容量（Capacity）：可以保存元素的最大数目，设置后不可改变
##限度（Limit）：限度小于容量，是可以保存数据的最后一个位置
##标记（Mark）：缓冲区中客户端专有的索引
#常见的方法：
##'public final Buffer clear()' 清空缓冲区并将position置为0
##'public final Buffer rewind()' 将位置置为0，不改变限度
##'public final Buffer flip()' 将限度设为当前位置，位置设为0
##'public final int remaining()' 返回position到limit之间的元素数
##'public final boolean hasRemaining()' 在remaining大于0时返回true
