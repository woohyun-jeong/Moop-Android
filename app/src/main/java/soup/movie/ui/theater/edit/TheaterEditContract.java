package soup.movie.ui.theater.edit;

import android.support.annotation.NonNull;

import java.util.List;

import soup.movie.data.model.TheaterCode;
import soup.movie.ui.BaseContract;

class TheaterEditContract {

    interface Presenter extends BaseContract.Presenter<View> {
        void onConfirmClicked(List<TheaterCode> selectedTheaters);
    }

    interface View extends BaseContract.View {
        void render(@NonNull TheaterEditViewState viewState);
    }
}