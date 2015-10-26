package hermione;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.processing.OperationManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

import java.util.Random;

/**
 * Created by zjh on 15/10/20.
 */
public class FopUtil {
    String bucketName;
    OperationManager operater;
    Auth auth;
    String fopcallbackurl;
    String mpsprefix;
    String ak;
    String sk;

    public FopUtil() {
        String ak = ConfigManager.getConfiguration("ak");
        String sk = ConfigManager.getConfiguration("sk");
        this.bucketName = ConfigManager.getConfiguration("bucket");
        this.fopcallbackurl = ConfigManager.getConfiguration("fopcallbackurl");
        this.auth = Auth.create(ak, sk);
        this.operater = new OperationManager(this.auth);
        this.mpsprefix = ConfigManager.getConfiguration("mpsprefix");
    }

    public FopUtil(String ak, String sk, String bucketName, String fopcallbackurl, String mpsprefix) {
        this.ak = ak;
        this.sk = sk;
        this.bucketName = bucketName;
        this.fopcallbackurl = fopcallbackurl;
        this.auth = Auth.create(ak, sk);
        this.operater = new OperationManager(this.auth);
        this.mpsprefix = mpsprefix;
    }

    private String getPipelineName() {
        Random rand = new Random();
        int n = rand.nextInt(4) + 1;
        return String.format("%s%d", this.mpsprefix, n);
    }

    //fop operation
    public String fopOperation(String key, String operation) {
        String notifyURL = this.fopcallbackurl;
        boolean force = true;
        //每一个账号有四个私有MPS队列,可以为每一个fop任务(比如文档转换任务)随机分配一个私有队列最大化利用资源
        String pipeline = getPipelineName();

        //http://developer.qiniu.com/docs/v6/api/reference/fop/pfop/pfop.html
        //force会强制覆盖之前生成的文件
        StringMap params = new StringMap().putNotEmpty("notifyURL", notifyURL)
                .putWhen("force", 1, force).putNotEmpty("pipeline", pipeline);

        //http://developer.qiniu.com/docs/v6/api/reference/fop/odconv.html
        String fops = operation;

        try {
            // 针对指定空间的文件触发 pfop 操作
            String id = operater.pfop(this.bucketName, key, fops, params);
            System.out.println(id);
            // 可通过下列地址查看处理状态信息。
            // 实际项目中设置 notifyURL,接受通知。通知内容和处理完成后的查看信息一致。
            return "http://api.qiniu.com/status/get/prefop?id=" + id;
        } catch (QiniuException e) {
            Response r = e.response;
            // 请求失败时简单状态信息
            System.out.println(r.toString());
            try {
                // 响应的文本信息
                System.out.println(r.bodyString());
            } catch (QiniuException e1) {
                //ignore
            }
            return "woops exception here";
        }
    }
}
