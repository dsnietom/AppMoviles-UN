package co.edu.unal.apiandroidfrutas;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import co.edu.unal.apiandroidfrutas.interfaces.ProductoAPI;
import co.edu.unal.apiandroidfrutas.models.Producto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    EditText edtCodigo;
    TextView tvNombre;
    TextView tvDescripcion;
    TextView tvPrecio;
    TextView tvIngresos;
    TextView tvUtilidad;
    ImageView imgProducto;
    Button btnBuscar;
    String ano;

    private Spinner spinner;
    private static final String[]paths = {"2011", "2012", "2013","2014","2015"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtCodigo=findViewById(R.id.edtCodigo);
        tvNombre=findViewById(R.id.tvNombre);
        tvDescripcion=findViewById(R.id.tvDescripcion);
        tvPrecio=findViewById(R.id.tvPrecio);
        tvIngresos=findViewById(R.id.tvIngresos);
        tvUtilidad=findViewById(R.id.tvUtilidad);
        imgProducto=findViewById(R.id.imgProducto);
        btnBuscar=findViewById(R.id.btnBuscar);


        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLUE);
                ((TextView) adapterView.getChildAt(0)).setTextSize(25);

                ano = (String) spinner.getAdapter().getItem(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                find(edtCodigo.getText().toString(), ano);
            }
        });
    }


    private void find(String codigo, String ano){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.datos.gov.co/resource/")
                .addConverterFactory(GsonConverterFactory.create()).build();

        ProductoAPI productoAPI = retrofit.create(ProductoAPI.class);
        Call<List<Producto>> call = productoAPI.find(codigo.toUpperCase(),ano);

        call.enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                try {
                    if (response.isSuccessful()){
                        Producto p=response.body().get(0);
                        String URL_IMG="https://raw.githubusercontent.com/dsnietom/FindHome/master/imgpublicas/"+p.getCultivos()+".jpg";
                        tvNombre.setText(p.getCultivos());
                        tvDescripcion.setText(p.getCosto_de_produccion_ha());
                        tvPrecio.setText(p.getPrecio_al_productor_kg());
                        tvIngresos.setText(p.getIngreso_bruto_produccion());
                        tvUtilidad.setText(p.getUtilidad());
                        Glide.with(getApplication()).load(URL_IMG).into(imgProducto);

                    }

                }catch (Exception exception){
                    Toast.makeText(MainActivity.this, "Producto no encontrado\n"+exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de conexion", Toast.LENGTH_SHORT).show();

            }
        });


    }
}