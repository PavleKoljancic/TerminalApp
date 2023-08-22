package test.designe.app.terminalapp.adapters;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import test.designe.app.terminalapp.R;
import test.designe.app.terminalapp.RegisterTerminalActivity;
import test.designe.app.terminalapp.models.Transporter;

public class TransporterViewAdapter extends RecyclerView.Adapter< TransporterViewAdapter.ViewHolder>{

    List<Transporter> transporters;
    RegisterTerminalActivity parent;
    public TransporterViewAdapter(List<Transporter> transporters, RegisterTerminalActivity parent) {
        this.transporters = transporters;
        this.parent = parent;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transporter_item, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemBtn.setText(transporters.get(position).getName());

    }



    @Override
    public int getItemCount() {

        return this.transporters.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final Button itemBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemBtn = itemView.findViewById(R.id.itemBtn);
            itemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());


                    builder.setMessage("Da li želite da pošaljete zahtjev za registraciju?")
                            .setTitle(   itemBtn.getText().toString());
                    builder.setPositiveButton("Da", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            parent.sendTerminalActivationRequets(transporters.get(getLayoutPosition()));
                        }
                    });
                    builder.setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

                    builder.show();
                }
            });
        }

        public Button getItemBtn() {
            return itemBtn;
        }


    }
}
