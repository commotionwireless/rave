package net.commotionwireless.ravedbtest2.ui;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.commotionwireless.ravedbtest2.R;
import net.commotionwireless.ravedbtest2.model.RaveNode;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add product list fragment if this is first creation
        if (savedInstanceState == null) {
            NodeListFragment fragment = new NodeListFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, NodeListFragment.TAG).commit();
        }
    }

    /** Shows the product detail fragment */
    public void show(RaveNode raveNode) {

        NodeFragment productFragment = NodeFragment.forNode(raveNode.getAddress());

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("RaveNode")
                .replace(R.id.fragment_container,
                        productFragment, null).commit();
    }
}
