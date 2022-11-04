import React, { Component, createRef } from "react";

import { upload_file } from "../api/request";

export class ProfilePictureUpdater extends Component {
  constructor(props) {
    super(props);

    this.state = {
      selected: undefined,
      isWaiting: false,
      output: undefined,
    };

    this.inputForm = createRef();
    this.postUpload = this.postUpload.bind(this);
  }

  postUpload(res) {
    if (!res || res.code !== 200) {
      this.setState({ output: "Something went wrong." });
    } else {
      const {user, setUser, visibility} = this.props;

      user.profilePic = res.results;
      setUser(user);
      visibility(false);
    }

    this.setState({isWaiting: false})
  }

  async uploadFIle() {
    this.setState({ isWaiting: true, output: undefined });
    const formData = new FormData(this.inputForm.current);
    await upload_file(formData, (e) => {
      this.postUpload(JSON.parse(e.target.response));
    })
  }

  render() {
    const { selected, isWaiting, output } = this.state,
      MAX = 5 * 1024 * 1024;
    return (
      <>
        <form
          action="/api/v1/upload"
          method="post"
          ref={this.inputForm}
          onSubmit={(e) => {
            e.preventDefault();
            this.uploadFIle();
          }}
          encType="multipart/form-data"
          className="container row center justify-start column-flex"
        >
          <div className="row center space-bet">
            <div className="container-title">Change Profile Picture</div>
            <button
              type="button"
              disabled={isWaiting}
              onClick={() => {
                this.props.visibility(false);
              }}
              className="icon close-btn"
              style={{ backgroundColor: "white" }}
            >
              close
            </button>
          </div>
          <hr
            className="row"
            style={{ height: "2px", backgroundColor: "white", margin: "4px" }}
          />
          <div
            className="row center"
            style={{ padding: "8px", margin: "10px" }}
          >
            <input
              type="file"
              name="file"
              disabled={isWaiting}
              id="pic_upload"
              accept=".jpg,.jpeg,.png"
              maxLength={5 * 1024 * 1024}
              onInput={(e) => {
                if (e.target.files[0].size <= MAX) {
                  let reader = new FileReader();
                  reader.addEventListener("loadend", (e) => {
                    this.setState({ selected: reader.result });
                  });
                  reader.readAsDataURL(e.target.files[0]);
                }
              }}
              style={{ display: "none" }}
            />
            {selected ? (
              <div className="row center column-flex">
                <div className="profile-logo center column-flex">
                  <img src={selected} alt="user" />
                </div>
                <label
                  type="button"
                  className="btn default-btn point-c"
                  htmlFor="pic_upload"
                  style={isWaiting ? {background: "var(--shiney-grey)", cursor: "default"} : {}}
                >
                  Reselect
                </label>
              </div>
            ) : (
              <div>
                <label
                  htmlFor="pic_upload"
                  className="center column-flex point-c"
                >
                  <span className="icon" style={{ fontSize: "10rem" }}>
                    upload_file
                  </span>
                  <div style={{ fontFamily: "lato" }}>Upload File</div>
                  <div style={{ fontFamily: "roboto", fontSize: "0.7rem" }}>
                    Max size 5mb of JPG, JPEG and PNG.
                  </div>
                </label>
              </div>
            )}
          </div>
          <hr
            className="row"
            style={{ height: "2px", backgroundColor: "white", margin: "4px" }}
          />
          {output && (
            <div className="row center">
              <div className="error">{output}</div>
            </div>
          )}
          <div className="form-end row center space-bet">
            <div></div>
            <button
              disabled={!selected || isWaiting}
              className="btn default-btn"
            >
              {isWaiting ? "Uploading" : "Upload"}
            </button>
          </div>
        </form>
      </>
    );
  }
}
