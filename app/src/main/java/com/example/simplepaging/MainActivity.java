package com.example.simplepaging;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Product> myList;
    ProductAdapter adapter;

    DocumentSnapshot lastVisible;
    FirebaseFirestore root = FirebaseFirestore.getInstance();

    private boolean isScrolling = false;
    private boolean isLastItemReached = false;
    private int limit = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        loadData();
    }

    @SuppressLint("CheckResult")
    private void initView() {

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myList = new ArrayList<>();
        adapter = new ProductAdapter(this, myList);
        recyclerView.setAdapter(adapter);
    }

    private void loadData() {
        //Truy vấn lần đầu
        Query query = root.collection("Product")
                .orderBy("Name", Query.Direction.ASCENDING)
                .limit(limit); //limit: giới hạn mỗi lần load dữ liệu

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Product product = new Product(document.get("Name").toString(), document.getId());
                        myList.add(product);
                    }
                    //Cập nhật recyclerview
                    adapter.notifyDataSetChanged();
                    //Dữ liệu cuối cùng của lần load trước
                    lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);

                    //Khi người dùng scroll xuống cuối recyclerview sẽ load tiếp
                    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (newState == SCROLL_STATE_DRAGGING) {
                                isScrolling = true;
                            }

                            LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                            int visibleItemCount = linearLayoutManager.getChildCount();
                            int totalItemCount = linearLayoutManager.getItemCount();

                            //Người dùng scroll đến cuối recyclerview
                            if (isScrolling && (firstVisibleItemPosition + visibleItemCount == totalItemCount) && !isLastItemReached) {
                                isScrolling = false;
                                //Câu truy vấn dữ liệu tiếp theo
                                Query nextQuery = root.collection("Product")
                                        .orderBy("Name", Query.Direction.ASCENDING)
                                        .startAfter(lastVisible) //Load dữ liệu tiếp tục sau lần load trước
                                        .limit(limit);

                                nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> t) {
                                        if (t.isSuccessful()) {
                                            for (DocumentSnapshot d : t.getResult()) {
                                                Product product = new Product(d.get("Name").toString(), d.getId());
                                                myList.add(product);
                                            }
                                            if (t.getResult().size() >= limit) {
                                                adapter.notifyDataSetChanged();
                                                lastVisible = t.getResult().getDocuments().get(t.getResult().size() - 1);
                                            }
                                            else {
                                                //Hết dữ liệu load
                                                isLastItemReached = true;
                                            }
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                        }
                    };
                    recyclerView.addOnScrollListener(scrollListener);
                }
            }
        });
    }
}