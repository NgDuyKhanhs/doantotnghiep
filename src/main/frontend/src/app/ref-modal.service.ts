import {ComponentRef, Injectable} from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Injectable({ providedIn: 'root' })
export class RefModalService {
  private isOpen = false;
  private modalRef?: ComponentRef<any>;
  constructor(private modalService: NgbModal) {}

  open(
    data: any,
    refModal?: any,
    modalData?: any,
    isSizeSm?: boolean,
    typeID?: any,
    disableWindowClass?: any,
    currencyID?: any,
    objectId?: any,
    accountingObjectId?: any,
    force?: any,
    strCodePrint?: any,
    codePrint?: any,
    sizeOverridePopup?: any,
    importExcell?: boolean,
    isCategorySync?: boolean
  ): any {
    this.close();
    if (!force) {
      if (this.isOpen) {
        return;
      }
    }
    this.isOpen = true;
    let modalRef;
    if (sizeOverridePopup) {
      modalRef = this.modalService.open(refModal, {
        size: 'lg',
        windowClass: 'width-80 margin-5',
        backdrop: 'static',
      });
    } else if (importExcell) {
      modalRef = this.modalService.open(refModal, {
        size: 'lg',
        windowClass: 'width-80 width-60 margin-5',
        backdrop: 'static',
      });
    } else if (isCategorySync) {
      modalRef = this.modalService.open(refModal, {
        size: 'lg',
        backdrop: 'static',
        windowClass: 'width-80',
      });
    } else {
      modalRef = this.modalService.open(refModal, {
        size: isSizeSm ? 'sm' : 'lg',
        windowClass: disableWindowClass ? disableWindowClass : codePrint ? 'width-25 margin-5' : 'width-80 margin-5',
        backdrop: 'static',
      });
    }
    modalRef.componentInstance.data = data;
    if (modalData) {
      modalRef.componentInstance.modalData = modalData;
    }
    if (typeID) {
      modalRef.componentInstance.typeID = typeID;
    }
    if (currencyID) {
      modalRef.componentInstance.currencyID = currencyID;
    }
    if (objectId) {
      modalRef.componentInstance.objectId = objectId;
    }
    if (accountingObjectId) {
      modalRef.componentInstance.accountingObjectId = accountingObjectId;
    }
    if (strCodePrint) {
      modalRef.componentInstance.strCodePrint = strCodePrint;
    }
    modalRef.result.then(
      result => {
        this.isOpen = false;
      },
      reason => {
        this.isOpen = false;
      }
    );
    return modalRef;
  }

  close(): void {
    if (this.modalRef) {
      try {
        this.modalRef.destroy();
      } catch (e) {
        console.error('Lỗi khi đóng modal:', e);
      } finally {
        this.modalRef = undefined;
      }
    }
  }


}
