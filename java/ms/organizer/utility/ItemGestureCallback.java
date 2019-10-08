package ms.organizer.utility;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class ItemGestureCallback extends ItemTouchHelper.Callback {
    private final ItemGestureHelperAdapter adapter;
    private final boolean enableDrag;
    private final boolean enableSwipe;

    public ItemGestureCallback(final ItemGestureHelperAdapter adapter, final boolean enableDrag, final boolean enableSwipe) {
        this.adapter = adapter;
        this.enableDrag = enableDrag;
        this.enableSwipe = enableSwipe;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return enableDrag;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return enableSwipe;
    }

    @Override
    public int getMovementFlags(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final RecyclerView.ViewHolder target) {
        adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());

        return true;
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int direction) {

    }

    public interface ItemGestureHelperAdapter {
        void onItemMove(final int from, final int to);
    }
}
