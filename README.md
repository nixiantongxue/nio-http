# nio-http

修复了apache httpclient/httpasyncclient 使用中的一些缺陷和限制，并作为HTTP 交互更优质的方案，
可以升级服务内存的网络交互，也可以对feign,euraka,oss-client 作定制优化。我知道的如网易对微服务
组件的优化就是如此。nio-http就是这些场景的轻量级优化方案。

初步的实验验证，初步使用nio-http对上传下载的场景优化，10倍+效率提升。

1. 不过分封装
	对原有使用apache httpclient/httpasyncclient 的升级友好；不提倡过分封装使得使用隔阂	


2. 不只是NIO 
	NIO 网络交互模式结合异步并发编程组件和内存优化组件，使得更快不仅来源于网络模型
	而是得益于编程模型+内存模型+线程模型的组合，组合使得更快伴随更稳定
	

3. 不仅是HTTP
	可以单独使用Callback 进行并发编程；单独使用 buffer 优化内存控制或者使用直接内存
	
	
By nixian, nixiantongxue@163.com

2021.09.15
	
	