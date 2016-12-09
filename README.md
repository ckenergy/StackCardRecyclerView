# StackCardRecyclerView
this is RecyclerView like S7 Recent task
<br/>
<br/>
<img src="./img/exp.jpg" width = "180" height = "300"  />

## View GIF ##

<img src="./img/gif_v.gif" width = "180" height = "300" />
<img src="./img/gif_h_in_n.gif" width = "180" height = "300"  /> 
<Br/>
<img src="./img/gif_h_f.gif" width = "180" height = "300" />
<img src="./img/gif_h_l.gif" width = "180" height = "300"  />

thanks for [CarouselLayoutManager](https://github.com/Azoft/CarouselLayoutManager) , I use his framework,but change a lot of layout , add can't scroll effect;

### There have two type Vertical and Horizontal ###
<br/> 
<img src="./img/v_c_exp.jpg" width = "180" height = "300" />
<img src="./img/h_out_n.jpg" width = "180" height = "300"  /> 

### Any Type have two stack order(in stack and out stack) and two number order(positive and negative) ###

#### Example Horizontal not circle ####

 * 1.in stack order, positive 
 * 2.out stack order, positive
<br/>
<img src="./img/h_in_p.jpg" width = "180" height = "300" />
<img src="./img/h_out_p.jpg" width = "180" height = "300"  />

 * 3.in stack order, negative 
 * 4.out stack order, negative
<br/>
<img src="./img/h_in_n.jpg" width = "180" height = "300" />
<img src="./img/h_out_n.jpg" width = "180" height = "300"  />

### There also have two number count type(less and more) for Vertical and Horizontal ###

It will be less count when width is bigger than height in Vertical layout and height is bigger than width is Horizontal layout.

 * 1.less have 7 item
 * 2.more have 9 item
<br/>
<img src="./img/v_less.jpg" width = "300" height = "180" align=top />
<img src="./img/v_c_exp.jpg" width = "180" height = "300"  /> 

### How to use? ###

- new StackCardLayoutManager default is in stack and positive,
you can change this order 

``` java
          StackCardLayoutManager stackCardLayoutManager = new StackCardLayoutManager(StackCardLayoutManager.VERTICAL,true, new StackCardPostLayout());
        stackCardLayoutManager.setStackOrder(StackCardLayoutManager.OUT_STACK_ORDER);
        stackCardLayoutManager.setNumberOrder(StackCardLayoutManager.NEGATIVE_ORDER);  recyclerView.setLayoutManager(stackCardLayoutManager);                
```

- add swip and swip listener 
<br/>
<img src="./img/gif_swip.gif" width = "180" height = "300"  />
``` java
          ItemTouchHelperCallBack itemTouchHelperCallBack = new ItemTouchHelperCallBack();
          itemTouchHelperCallBack.setOnSwipListener(swipListener);
          ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallBack);
          itemTouchHelper.attachToRecyclerView(recyclerView);                
```

- add scroll to position and center listener 
<br/>
<img src="./img/h_scroll2p.gif" width = "180" height = "300"  />
``` java
          // enable center post touching on item and item click listener
        DefaultChildSelectionListener.initCenterItemListener(new DefaultChildSelectionListener.OnCenterItemClickListener() {
            @Override
            public void onCenterItemClicked(@NonNull final RecyclerView recyclerView, @NonNull final StackCardLayoutManager stackCardLayoutManager, @NonNull final View v) {
                final int position = recyclerView.getChildLayoutPosition(v);
                final String msg = String.format(Locale.US, "Item %1$d was clicked", position);
                Log.d("onCenterItemClicked", msg);
                Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        }, recyclerView, layoutManager);                
```

### Support (支持) ###
- Please do not send e-mail to me. Public technical discussion on github is preferred.
- 请尽量在 github 上公开讨论[技术问题](https://github.com/ckenergy/StackCardRecyclerView/issues)，不要以邮件方式私下询问。

### TODO ###

* 1.overDraw need fix
* 2.need more faster (although it have fast scroll,when i scroll more faster the view aren't so fast)
* 3.my english : )

#### License ####

	Copyright 2016 ckenergy <ckenergy@qq.com>
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.