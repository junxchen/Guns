package cn.stylefeng.guns.core.util;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;

/**
 * @author xuyuxiang
 * @name: SvgUtil
 * @description: svgutil
 * @date 2019/10/2511:56
 */
public class SvgUtil {

    public static void saveImage(String svgData) throws Exception{
        PNGTranscoder coder=new PNGTranscoder();
        svgData="<?xml version=\"1.0\" encoding=\"utf-8\"?>"+svgData;
        //ByteArrayInputStream fin=new ByteArrayInputStream(svgData.getBytes());
        StringReader reader=new StringReader(svgData);
        TranscoderInput input=new TranscoderInput(reader);
        File f=new File("d:");
        if(!f.exists())f.mkdirs();
        FileOutputStream fout=new FileOutputStream("process.png");
        TranscoderOutput output=new TranscoderOutput(fout);
        try{
            coder.transcode(input, output);
        }finally{
            reader.close();
            //fin.close();
            fout.close();
        }
    }
}
