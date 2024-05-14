package test.designe.app.terminalapp.retrofit;

import com.google.gson.Gson;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Converter;

public class RetrofitService {





     private static Retrofit retrofit;

    private static DriverAPI api;

    public static DriverAPI getApi()
    {
        if(api==null)
        {
            if(retrofit==null)
                initializeRetrofit();
           api = retrofit.create(DriverAPI.class);
        }

        return api;
    }

    private static void initializeRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(MyURL.getURL())
                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new Gson()))

                .build();
    }


   static class NullOnEmptyConverterFactory extends Converter.Factory {
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(
                final java.lang.reflect.Type type,
                final java.lang.annotation.Annotation[] annotations,
                final Retrofit retrofit) {
            final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
            return new Converter<ResponseBody, Object>() {
                @Override
                public Object convert(ResponseBody body) throws IOException {
                    if (body.contentLength() == 0) {
                        return null;
                    }
                    return delegate.convert(body);
                }
            };
        }
    }


}

