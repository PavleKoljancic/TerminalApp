package test.designe.app.terminalapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import test.designe.app.terminalapp.ChooseRouteActivity;
import test.designe.app.terminalapp.MainActivity;
import test.designe.app.terminalapp.R;
import test.designe.app.terminalapp.models.Route;
import test.designe.app.terminalapp.sigeltons.RouteSingelton;

public class RouteViewAdapter extends RecyclerView.Adapter< RouteViewAdapter.ViewHolder>{

    List<Route> routes;
    ChooseRouteActivity parent;
    public RouteViewAdapter(List<Route> routes, ChooseRouteActivity parent) {
        this.routes = routes;
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
        holder.itemBtn.setText(routes.get(position).getName());

    }



    @Override
    public int getItemCount() {

        return this.routes.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final Button itemBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemBtn = itemView.findViewById(R.id.itemBtn);
            itemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    RouteSingelton.setRoute(routes.get(getLayoutPosition()));
                    Intent intent = new Intent(parent, MainActivity.class);
                    parent.startActivity(intent);
                    parent.finish();

                }
            });
        }

        public Button getItemBtn() {
            return itemBtn;
        }


    }
}
