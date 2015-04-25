缓冲区
==========

总述
----------
流和通道的区别在于前者基于字节而后者基于块，NIO使用了通道作为传输手段而Buffer则作为块的构造方式。Java中，每种基本类型都对应着一种Buffer。

对于任何一种Buffer，都有4个部分的关键信息：
  * 位置（Position）：索引记录将要写入或读取的下一个位置
  * 容量（Capacity）：可以保存元素的最大数目，设置后不可改变
  * 限度（Limit）：限度小于容量，是可以保存数据的最后一个位置
  * 标记（Mark）：缓冲区中客户端专有的索引

常见的方法：
  * `public final Buffer clear()` 清空缓冲区并将position置为0
  * `public final Buffer rewind()` 将位置置为0，不改变限度
  * `public final Buffer flip()` 将限度设为当前位置，位置设为0
  * `public final int remaining()` 返回position到limit之间的元素数
  * `public final boolean hasRemaining()` 在remaining大于0时返回true
  
创建缓冲区
----------
缓冲区的类基于继承，但不基于多态，至少在顶层如此。
每种类型的缓冲区都有工厂方法，`allocate`分配方法一般用于输入缓冲的创建，`wrap`包装方法一般用于输出缓冲的创建。

* __分配__：例如`allocate(100)`将会返回一个大小为100，position为0的空缓冲区。
* __直接分配__：`ByteBuffer`类有一个`allocateDirect(int capacity)`的方法，不创建后台数组，VM直接在网卡、核心内存等位置的缓冲区上直接进行内存访问，不能使用缓冲区的`array()`以及`arrayOffset()`方法，在缓冲区容量很大导致性能实在无法满足需要时可以考虑使用，否则不推荐。
* __包装__：现在发送的数据一经准备好，使用`warp(byte[] data)`方法可以直接把数据包装为ByteBuffer，__注意：这个数组会作为引用传入，之后对数组的操作将会反映到ByteBuffer中！__。
