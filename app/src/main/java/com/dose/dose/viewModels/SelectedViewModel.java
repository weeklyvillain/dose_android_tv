package com.dose.dose.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dose.dose.content.BaseContent;

public class SelectedViewModel extends ViewModel {
    private final MutableLiveData<BaseContent> selected = new MutableLiveData<BaseContent>();

    public void setSelected(BaseContent sel) {
        selected.setValue(sel);
    }

    public LiveData<BaseContent> getSelected() {
        return selected;
    }
}
